namespace org.mycore.mets.controller {
    export class ErrorModalController  {
        constructor($scope, private $modalInstance, public i18nModel) {
            $scope.ctrl = this;
            this.errorModel = $modalInstance.errorModel;
        }

        public errorModel: org.mycore.mets.model.ErrorModalModel;

        public okayClicked(event:JQueryMouseEventObject) {
            this.$modalInstance.close({});
        }

    }
}

angular.module("ErrorModal", [ "ui.bootstrap", "MetsEditorI18NModel" ]).controller("ErrorModalController",
    ["$scope", "$modalInstance", "MetsEditorI18NModel", org.mycore.mets.controller.ErrorModalController]);
