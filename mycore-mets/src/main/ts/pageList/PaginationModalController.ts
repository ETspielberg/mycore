namespace org.mycore.mets.controller.Pagination {

    import PaginationModalModel = org.mycore.mets.model.PaginationModalModel;

    export class PaginationModalController {
        constructor($scope, private $modalInstance) {
            this.model = <org.mycore.mets.model.PaginationModalModel>($modalInstance.model);
            $scope.ctrl = this;
            $scope.$watch("ctrl.model.begin", ()=>this.doChanges());
            $scope.$watch("ctrl.model.method", ()=>this.doChanges());
            $scope.$watch("ctrl.model.reverse", ()=>this.doChanges());
            $scope.$watch("ctrl.model.value", ()=> {
                this.changeType();
                this.doChanges();
            });
        }

        private changes = new Array<{oldLabel:string;newLabel:string;page:org.mycore.mets.model.simple.MCRMetsPage}>();

        public doChanges() {
            var changesLeft = true;

            while (changesLeft) {
                changesLeft = this.changes.pop() != null;
            }

            this.calculateChanges().forEach(c=>this.changes.push(c));
        }

        public changeType() {
            var value = this.model.value;
            if (value != null) {
                var newMethod = org.mycore.mets.model.Pagination.detectPaginationMethodByPageLabel(value);
                if (newMethod != null) {
                    this.model.method = newMethod;
                }
            }
        }

        changeClicked(page:org.mycore.mets.model.simple.MCRMetsPage, index:number) {
            this.model.begin = index;
        }

        public calculateChanges(replaceOldLabel = true) {
            var changes;
            if (this.model.method != null && this.model.method.test(this.model.value)) {
                changes = org.mycore.mets.model.Pagination.getChanges(
                    0,
                    this.model.selectedPages.length,
                    this.model.begin,
                    this.model.value,
                    this.model.method,
                    this.model.reverse
                );
            } else {
                changes = this.model.selectedPages.map(()=>"");
            }

            return this.model.selectedPages.map((page:org.mycore.mets.model.simple.MCRMetsPage, index:number)=> {
                var oldLabel;
                if (replaceOldLabel) {
                    var pageNumber = (this.model.selectedPagesIndex + 1 + index);
                    var alternativeLabel = (this.model.messages[ "noOrderLabel" ] + "(" + pageNumber + ")");
                    oldLabel = page.orderLabel || alternativeLabel;
                } else {
                    oldLabel = page.orderLabel;
                }
                return {
                    oldLabel : oldLabel,
                    newLabel : changes[ index ],
                    page : page
                };
            });
        }

        public change(){
            this.$modalInstance.close(this.calculateChanges(false).map((change)=> {
                return new org.mycore.mets.model.state.PageLabelChange(change.page, change.newLabel, change.oldLabel);
            }));
        }

        public abort(){
            this.$modalInstance.dismiss();
        }

        public model:PaginationModalModel;
    }
}


angular.module("MetsEditorApp")
    .controller("PaginationModalController", ["$scope",  "$modalInstance",org.mycore.mets.controller.Pagination.PaginationModalController]);
