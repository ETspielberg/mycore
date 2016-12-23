namespace org.mycore.mets.model {

    import MetsModel = org.mycore.mets.model.simple.MCRMetsSimpleModel;
    /**
     * This is the service which loads the mets file and parses it into simple model.
     */
    export class MetsModelLoader {
        constructor(private httpService) {
        }

        load(url:string, callBack:(model:MetsModel)=>void) {
            var promise = this.httpService.get(url);

            promise.success((metsData) => {
                callBack(MetsModel.fromJson(metsData));
            });

            promise.error(()=>{
                // TODO: ERROR HANDLING
            });
        }
    }
}

angular.module("MetsModelLoaderService", []).service("MetsModelLoaderService",
    ["$http", ($http)=> new org.mycore.mets.model.MetsModelLoader($http) ]);
