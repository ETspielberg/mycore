///<reference path="MCRMetsSection.ts"/>
///<reference path="MCRMetsPage.ts"/>

namespace org.mycore.mets.model.simple {
    export class MCRMetsSimpleModel {

        constructor(public rootSection:MCRMetsSection,
                    public metsPageList:Array<MCRMetsPage>) {
        }


        public static fromJson(json:any):MCRMetsSimpleModel {
            var serializedJson = (typeof json != "object") ? JSON.parse(json) : json;

            var idPageMap = {};
            var idFileMap = {};
            var idChapterMap = {};

            var metsPageList = serializedJson.metsPageList.map((pageObject, index)=> {
                var files = pageObject.fileList.map((fileObject:MCRMetsFile)=> {
                    return new MCRMetsFile(fileObject.id, fileObject.href, fileObject.mimeType, fileObject.use);
                });
                var mcrMetsPage = new MCRMetsPage(pageObject.id,
                    pageObject.orderLabel || null, pageObject.contentIds || null,
                    pageObject.hidden || false,
                    files);
                mcrMetsPage.fileList.forEach((file:MCRMetsFile)=> {
                    idFileMap[ file.id ] = file;
                });
                idPageMap[ pageObject.id ] = mcrMetsPage;
                return mcrMetsPage;
            });


            var metsRootSection = MCRMetsSimpleModel.createSection(serializedJson.rootSection, idChapterMap, idFileMap);
            serializedJson.sectionPageLinkList.forEach((linkObject)=> {
                var page = idPageMap[ linkObject.to ];
                var section = idChapterMap[ linkObject.from ];
                section.linkedPages.push(page);
            });
            return new MCRMetsSimpleModel(metsRootSection, metsPageList);
        }

        private static createSection(object:any, idChapterMap:any, idFileMap:any):MCRMetsSection {
            var section = new MCRMetsSection(object.id, object.type, object.label);
            idChapterMap[ section.id ] = section;

            object.metsSectionList.forEach((childObject)=> {
                var child = MCRMetsSimpleModel.createSection(childObject, idChapterMap, idFileMap);
                section.metsSectionList.push(child);
                child.parent = section;
            });

            if ("altoLinks" in object) {
                object.altoLinks.forEach((altoLink)=> {
                    if (typeof altoLink.altoFile == "undefined" || !(altoLink.altoFile in idFileMap) ||
                        typeof altoLink.begin == "undefined" || typeof altoLink.end == "undefined") {
                        console.warn("invalid alto-link ");
                        console.warn(altoLink);
                        return;
                    }
                    var al = new MCRMetsAltoLink(idFileMap[ altoLink.altoFile ], altoLink.begin, altoLink.end);
                    section.altoLinks.push(al);
                });
            }


            return section;
        }


        public static toJson(model:MCRMetsSimpleModel) {
            var sectionIdPageMap = new Array();
            var addIdsToSectionMap = (section:MCRMetsSection)=> {
                section.metsSectionList.forEach(addIdsToSectionMap);
                section.linkedPages.forEach((lp:MCRMetsPage)=> {
                    sectionIdPageMap.push({from : section.id, to : lp.id});
                });
            };
            addIdsToSectionMap(model.rootSection);

            var pageList = model.metsPageList.map((p:MCRMetsPage)=>MCRMetsPage.copy(p));
            var root = model.rootSection.getJsonObject();

            return JSON.stringify({
                sectionPageLinkList : sectionIdPageMap,
                metsPageList : pageList,
                rootSection : root
            });
        }
    }


}
