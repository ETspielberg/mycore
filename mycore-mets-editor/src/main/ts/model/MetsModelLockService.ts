namespace org.mycore.mets.model {

    import MetsModel = org.mycore.mets.model.simple.MCRMetsSimpleModel;
    import MCRMetsPage = org.mycore.mets.model.simple.MCRMetsPage;
    import MCRMetsSection = org.mycore.mets.model.simple.MCRMetsSection;
    /**
     * This is the service which loads the mets file and parses it into simple model.
     */
    export class MetsModelLock {
        constructor(private httpService) {
        }

        lock(lockURL:string, callBack:(success:boolean)=>void) {
            if(typeof lockURL != "undefined" && lockURL !== null){
                var promise = this.httpService.get(lockURL);

                promise.success((data) => {
                    callBack(data.success || false);
                });

                promise.error(()=> {
                    callBack(false);
                });
            } else {
                callBack(true);
            }
        }

        unlock(unLockURL){
            if(typeof unLockURL != "undefined" && unLockURL !== null){
                var promise = this.httpService.get(unLockURL);
            }
        }
    }
}

angular.module("MetsModelLockService", []).service("MetsModelLockService",
    [ "$http", ($http)=> new org.mycore.mets.model.MetsModelLock($http) ]);
