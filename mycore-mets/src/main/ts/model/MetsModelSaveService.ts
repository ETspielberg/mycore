namespace org.mycore.mets.model {

    import MetsModel = org.mycore.mets.model.simple.MCRMetsSimpleModel;
    import MCRMetsPage = org.mycore.mets.model.simple.MCRMetsPage;
    import MCRMetsSection = org.mycore.mets.model.simple.MCRMetsSection;
    /**
     * This is the service which loads the mets file and parses it into simple model.
     */
    export class MetsModelSave {
        constructor(private httpService) {
        }

        save(url:string, model:MetsModel, callBack:(success:boolean)=>void) {
            var jsonData = MCRMetsSimpleModel.toJson(model);
            console.log(jsonData);
            var promise = this.httpService.post(url, jsonData);

            promise.success(() => {
                callBack(true);
            });

            promise.error(()=> {
                callBack(false);
            });
        }
    }
}

angular.module("MetsModelSaveService", []).service("MetsModelSaveService",
    [ "$http", ($http)=> new org.mycore.mets.model.MetsModelSave($http) ]);
