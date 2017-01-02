namespace org.mycore.mets.controller {

    import MetsEditorModel = org.mycore.mets.model.MetsEditorModel;
    import MetsEditorModelFactory = org.mycore.mets.model.MetsEditorModelFactory;

    export class MetsEditorController {
        constructor(private $scope,
                    private metsEditorModelFactory:MetsEditorModelFactory,
                    private i18nModel,
                    hotkeys,
                    $timeout,
                    private metsModelLockService:org.mycore.mets.model.MetsModelLock,
                    private $modal,
                    private $window) {
            this.initHotkeys(hotkeys);

            $scope.$on("AddedSection", (event, data)=> {
                $timeout(()=> {
                    $scope.$broadcast("editSection", {
                        section : data.addedSection
                    });
                });

            });
        }

        private privateErrorModal;

        private initHotkeys(hotkeys) {
            hotkeys.add({
                combo : "ctrl+1",
                description : "",
                callback : ()=> {
                    this.model.mode = MetsEditorModel.EDITOR_PAGINATION;
                }
            });

            hotkeys.add({
                combo : "ctrl+2",
                description : "",
                callback : ()=> {
                    this.model.mode = MetsEditorModel.EDITOR_STRUCTURING;
                }
            });

        }

        public init(parameter:MetsEditorParameter) {
            this.validate(parameter);
            this.model = this.metsEditorModelFactory.getInstance(parameter);
            this.metsModelLockService.lock(this.model.lockURL, (success:boolean)=> {
                if (!success) {
                    var options = {
                        templateUrl : "error/modal.html",
                        controller : "ErrorModalController",
                        size : "sm"
                    };

                    this.privateErrorModal = this.$modal.open(options);
                    this.privateErrorModal.errorModel = new org.mycore.mets.model.ErrorModalModel(
                        this.i18nModel.messages[ "noLockTitle" ] || "???noLockTitle???",
                        this.i18nModel.messages[ "noLockMessage" ] || "???noLockMessage???"
                    );
                    var emptyCallback = () => {
                        //do nothing
                    };
                    this.privateErrorModal.result.then(emptyCallback, emptyCallback);
                } else {
                    this.model.locked = true;

                    this.$window.onbeforeunload = ()=>{
                        this.metsModelLockService.unlock(this.model.unLockURL);
                        if (!this.model.stateEngine.isServerState()) {
                            return this.i18nModel.messages[ "notSaved" ];
                        }
                    };
                }
            });

        }

        public close(){
            this.$window.close();
        }

        public viewOptionClicked(option:string) {
            this.model.middleView = option;
        }


        private validate(parameter:MetsEditorParameter):void {
            this.checkParameter("metsId", parameter);
            this.checkParameter("sourceMetsURL", parameter);
            this.checkParameter("targetServletURL", parameter);

        }

        private checkParameter(parameterToCheck:string, parameters:MetsEditorParameter) {
            if (!(parameterToCheck in parameters) || parameters[ parameterToCheck ] == null) {
                throw `MetsEditorParameter does not have a valid ${parameterToCheck}`;
            }
        }

        private model:MetsEditorModel;
    }

}
{
    let iModule = angular.module("MetsEditorApp",
        [ "MetsEditorI18NModel",
            "fa.directive.borderLayout",
            "MetsEditorTemplates",
            "ngDraggable",
            "ui.bootstrap",
            "ErrorModal",
            "MetsEditorSectionModule",
            "MetsEditorModelFactory",
            "MetsModelSaveService",
            "MetsEditorPresenterModule",
            "ng-image-zoom",
            "cfp.hotkeys",
            "afkl.lazyImage",
            "MetsModelLockService"
        ]);
    iModule.directive("selectImmediately", [ "$timeout", function ($timeout) {
        return {
            restrict : "A",
            link : function (scope, iElement:any) {
                $timeout(function () { // Using timeout to let template to be appended to DOM prior to select action.
                    iElement[ 0 ].select();
                });
            }
        };
    } ]);
    iModule.controller("MetsEditorController",
        [ "$scope", "MetsEditorModelFactory", "MetsEditorI18NModel", "hotkeys", "$timeout", "MetsModelLockService", "$modal", "$window",
            org.mycore.mets.controller.MetsEditorController ]);
}
