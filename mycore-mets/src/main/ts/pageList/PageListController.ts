namespace org.mycore.mets.controller {
    import MetsEditorModel = org.mycore.mets.model.MetsEditorModel;
    import PagesMoveChange = org.mycore.mets.model.state.PagesMoveChange;

    export class PageListController {

        constructor(ngDraggable, private timeout, modal, hotkeys, i18NModel) {
            this.messages = i18NModel.messages;
            hotkeys.add({
                combo : "up",
                description : "",
                callback : ()=> {
                    var pageSelection = this.model.pageSelection;
                    if (pageSelection.from == null) {
                        pageSelection.from = pageSelection.to = this.model.metsModel.metsPageList.length - 1;
                    } else if (pageSelection.from == 0) {
                        pageSelection.from = pageSelection.to = null;
                    } else {
                        pageSelection.to = pageSelection.from = pageSelection.from - 1;
                        return false;
                    }
                }
            });

            hotkeys.add({
                combo : "down",
                description : "",
                callback : ()=> {
                    var pageSelection = this.model.pageSelection;
                    if (pageSelection.from == null) {
                        pageSelection.from = pageSelection.to = 0;
                    } else if (pageSelection.from >= this.model.metsModel.metsPageList.length) {
                        pageSelection.from = pageSelection.to = null;
                    } else {
                        pageSelection.to = pageSelection.from = pageSelection.from + 1;
                        return false;
                    }
                }
            });
        }

        private messages:Array<string>;
        private model:MetsEditorModel;
        private prevent = true;

        public thumbnails:boolean = false;
        public editable:boolean;

        public init(model:MetsEditorModel, editable:boolean = true, thumbnails = false) {
            this.model = model;
            this.editable = editable;
            this.thumbnails = thumbnails;
        }

        public getPageList() {
            return this.model.metsModel.metsPageList;
        }

        public getPageIndex(page:model.simple.MCRMetsPage) {
            return this.model.metsModel.metsPageList.indexOf(page);
        }

        public isPageSelected(page:model.simple.MCRMetsPage) {
            var pageIndex = this.getPageIndex(page);
            return this.model.pageSelection.from != null &&
                this.model.pageSelection.from <= pageIndex &&
                this.model.pageSelection.to >= pageIndex;
        }

        public isPageAloneSelected(page:model.simple.MCRMetsPage) {
            var pageIndex = this.getPageIndex(page);
            return (this.model.pageSelection.from == this.model.pageSelection.to) && (this.model.pageSelection.from == pageIndex);
        }

        public getSelectedPages() {
            var selectedPageFilter = (page)=> this.isPageSelected(page);
            return this.model.metsModel.metsPageList.filter(selectedPageFilter);
        }

        public pageClicked(page, event) {

            // prevent selection
            if (this.prevent) {
                this.prevent = false;
                return;
            }

            if (angular.element(event.target).is("[type='text']")) {
                return;
            }

            document.getSelection().removeAllRanges();
            var pageIndex = this.getPageIndex(page);
            if (this.model.pageSelection.from == null || !event.shiftKey) {
                // user doest used shift key
                if (this.model.pageSelection.from !== pageIndex && this.model.pageSelection.to !== pageIndex) {
                    this.model.pageSelection.from = pageIndex;
                    this.model.pageSelection.to = pageIndex;
                } else {
                    this.model.pageSelection.from = this.model.pageSelection.to = null;
                }
            } else {
                event.preventDefault();
                event.stopPropagation();
                // used shift key
                if (this.model.pageSelection.from < pageIndex && pageIndex < this.model.pageSelection.to) {
                    // user clicked in range
                    if (this.model.pageSelection.lastExpand == "top") {
                        this.model.pageSelection.from = pageIndex;
                    } else {
                        this.model.pageSelection.to = pageIndex;
                    }
                } else {
                    // user clicked out of page range
                    if (this.model.pageSelection.from > pageIndex) {
                        // user clicked above selection
                        this.model.pageSelection.from = pageIndex;
                        this.model.pageSelection.lastExpand = "top";
                    } else {
                        // user clicked under selection
                        this.model.pageSelection.to = pageIndex;
                        this.model.pageSelection.lastExpand = "bottom";
                    }
                }
            }
        }

        dropSuccess(element, position, data, event) {
            var metsPageList = this.model.metsModel.metsPageList;

            if (typeof this.model.pageSelection == "undefined" ||
                this.model.pageSelection == null ||
                this.model.pageSelection.from == null ||
                this.model.pageSelection.to == null ||
                (!(this.model.pageSelection.from in metsPageList)) ||
                (!(this.model.pageSelection.to in metsPageList))
            ) {
                throw new Error("invalid selection!");
            }

            var fromPage = metsPageList[ this.model.pageSelection.from ];
            var toPage = metsPageList[ this.model.pageSelection.to ];

            var range = {from : fromPage, to : toPage};
            var target = {before : position == "before", element : element};
            console.log(`range  { from: ${fromPage.orderLabel}, to: ${toPage.orderLabel}  }`);
            console.log(`target { before: ${target.before}, element: ${target.element.orderLabel}}`);

            this.model.stateEngine.changeModel(new PagesMoveChange(metsPageList, range, target));

            this.model.pageSelection.from = this.model.pageSelection.to = null;
            var back = this.model.mode;
            var selection = {from:this.model.pageSelection.from, to:this.model.pageSelection.to };
        }

        public onDragComplete(data, event) {
            // workarround because click is triggered before drop success
            this.prevent = true;
        }

    }
}
{
    let pageListModule = angular.module("MetsEditorApp");
    pageListModule.controller("MetsEditorPageListController",
        [ "ngDraggable", "$timeout", "$modal", "hotkeys", "MetsEditorI18NModel", org.mycore.mets.controller.PageListController ]);

    pageListModule.directive("jumpToElement", ["$timeout", function ($timeout) {
        return {
            restrict : "A",
            link : function (scope, iElement, attr) {
                var findOverflowScrollParent = (elem)=> {
                    var ngElement = elem;
                    if (ngElement.css("overflow") == "scroll" || ngElement.css("overflow-y") == "scroll") {
                        return ngElement;
                    } else if (ngElement.parent() != null) {
                        return findOverflowScrollParent(ngElement.parent());
                    } else {
                        return null;
                    }
                };
                var jumpToElement = function () {
                    $timeout(function () { // Using timeout to let template to be appended to DOM prior to select action.
                        var elemToScrollTo = iElement;
                        var scrollParent = findOverflowScrollParent(iElement);
                        if (scrollParent != null) {
                            var ngScrollParent = scrollParent;
                            var parentTop = ngScrollParent.position().top;
                            var elementTop = elemToScrollTo.position().top;
                            var scrollTop = ngScrollParent[ 0 ].scrollTop;
                            var offset = -1 * (scrollTop - elementTop);

                            var parentHeight = ngScrollParent.height();

                            if (offset > parentHeight - elemToScrollTo.height()) {
                                offset -= (parentHeight - 2 * elemToScrollTo.height());
                            } else if (offset < parentHeight && offset > 0) {
                                return;
                            }
                            ngScrollParent[ 0 ].scrollTop = ngScrollParent[ 0 ].scrollTop + offset;
                        }
                    });
                };

                var checkAndJump = function () {
                    if ((<any>attr).jumpToElement == "true") {
                        jumpToElement();
                    }
                };

                checkAndJump();
                attr.$observe("jumpToElement", function (val) {
                    checkAndJump();
                });
            }
        };
    }]);
}
