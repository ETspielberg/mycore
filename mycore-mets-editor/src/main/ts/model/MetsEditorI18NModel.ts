angular.module("MetsEditorI18NModel", [ "MetsEditorConfiguration" ])
    .factory("MetsEditorI18NModel", [ "$http", "$location", "$log", "MetsEditorConfiguration",
        ($http:ng.IHttpService,
         $location:ng.ILocationService,
         $log:ng.ILogService, editorConfiguration:MetsEditorConfiguration) => {
            var metsEditorMessageModel = {messages : new Array()};

            $http.get(editorConfiguration.i18URL).success((i18nData) => {
                for (var index in i18nData) {
                    if (i18nData.hasOwnProperty(index)) {
                        var betterKey = index;
                        if(index.startsWith("component.mets.editor")){
                            betterKey = index.substr("component.mets.editor.".length);
                        } else if (index.startsWith("component.mets.dfgStructureSet")) {
                            betterKey = index.substr("component.mets.dfgStructureSet.".length);
                        }
                        metsEditorMessageModel.messages[ betterKey ] = i18nData[ index ];
                    }
                }
            });

            return metsEditorMessageModel;
        } ]);
