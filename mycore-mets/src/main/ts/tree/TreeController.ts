///<reference path="../../../../typings/angularjs/angular.d.ts"/>
///<reference path="../../../../typings/jquery/jquery.d.ts"/>

import MCRMetsSection = org.mycore.mets.model.simple.MCRMetsSection;
import MetsEditorTreeModel = org.mycore.mets.model.MetsEditorTreeModel;
import DropTarget = org.mycore.mets.model.DropTarget;
import StateEngine = org.mycore.mets.model.state.StateEngine;
import MCRMetsSimpleModel = org.mycore.mets.model.simple.MCRMetsSimpleModel;
import MetsEditorModel = org.mycore.mets.model.MetsEditorModel;

namespace org.mycore.mets.controller {
    /**
     * The MCRTreeController is used to display a tree of elements which can be sorted.
     * You nee to call the init method.
     */
    export class MCRTreeController {
        constructor(private $scope,
                    i18nModel,
                    ngDraggable,
                    private $modal,
                    editorConfiguration:MetsEditorConfiguration) {
            this.treeModel = new MetsEditorTreeModel();
            this.messageModel = i18nModel;
            this.metsConfiguration = editorConfiguration;
        }

        private privateChildFieldName:string;
        private privateParentFieldName:string;
        private privateErrorModal:any;
        private messageModel;
        private metsConfiguration:MetsEditorConfiguration;
        public treeModel:MetsEditorTreeModel;
        private stateEngine:StateEngine;
        private simpleModel:MCRMetsSimpleModel;
        private editorModel:MetsEditorModel;

        public init(root:any, childFieldName:string, parentFieldName:string, metsEditorModel:MetsEditorModel) {
            if (root == null || typeof root == "undefined") {
                throw `invalid
                root
                parameter
                ${root}
            `;
            } else if (childFieldName == null || childFieldName == "" || typeof childFieldName == "undefined") {
                throw `invalid
                childFieldName
                parameter
                ${childFieldName}
            `;
            } else if (parentFieldName == null || parentFieldName == "" || typeof parentFieldName == "undefined") {
                throw `invalid
                parentFieldName
                parameter
                ${parentFieldName}
            `;
            }

            this.treeModel.root = root;
            this.privateChildFieldName = childFieldName;
            this.privateParentFieldName = parentFieldName;
            this.stateEngine = metsEditorModel.stateEngine;
            this.simpleModel = metsEditorModel.metsModel;
            this.editorModel = metsEditorModel;
        }

        public getChildren(child:any) {
            const childList = child[ this.privateChildFieldName ];
            return childList;
        }

        public getChildrenCount(child:any) {
            const length = this.getChildren(child).length;
            return length;
        }

        public clickFolder(element:any, event:JQueryMouseEventObject) {
            event.stopImmediatePropagation();
            event.stopPropagation();
            this.treeModel.setElementOpen(element, !this.treeModel.getElementOpen(element));
        }

        public buildDropTarget(element:any, position):DropTarget {
            return new DropTarget(element, position);
        }

        public dropSuccess(target:DropTarget, sourceElement:MCRMetsSection, event:JQueryEventObject) {
            let realTargetElement;

            if (target.position == "after" || target.position == "before") {
                realTargetElement = target.element[ this.privateParentFieldName ];
            } else {
                realTargetElement = target.element;
            }

            if (target.element == sourceElement || realTargetElement == sourceElement) {
                return true;
            }

            if (!this.checkConsistent(realTargetElement, sourceElement) && target.element) {
                const options = {
                    templateUrl : "error/modal.html",
                    controller : "ErrorModalController",
                    size : "lg"
                };

                this.privateErrorModal = this.$modal.open(options);
                this.privateErrorModal.errorModel = new org.mycore.mets.model.ErrorModalModel(
                    this.messageModel.messages[ "errorMoveChildTitle" ] || "???errorMoveChildTitle???",
                    this.messageModel.messages[ "errorMoveChildMessage" ] || "???errorMoveChildMessage???",
                    this.metsConfiguration.resources + "img/move_parent_to_child.png"
                );
                const emptyCallback = () => {
                    //do nothing
                };
                this.privateErrorModal.result.then(emptyCallback, emptyCallback);

                return true;
            }

            const sectionMoveChange = new org.mycore.mets.model.state.SectionMoveChange(sourceElement, target);
            this.stateEngine.changeModel(sectionMoveChange);

            return true;
        }

        private checkConsistent(path:any, source:any) {
            if (path[ this.privateParentFieldName ] == source) {
                return false;
            } else {
                return path[ this.privateParentFieldName ] == null || this.checkConsistent(path[ this.privateParentFieldName ], source);
            }
        }
    }
}





