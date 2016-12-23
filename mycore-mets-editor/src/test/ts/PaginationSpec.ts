///<reference path="../../../typings/jasmine/jasmine.d.ts"/>
module org.mycore.mets.tests {
    describe("Pagination", ()=> {

        var paginationExample = {
            arabicNumbering : [ "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" ],
            rectoVerso_lowercase : [ "1r", "1v", "2r", "2v", "3r", "3v", "4r", "4v", "5r", "5v" ],
            ab_lowercase : [ "1a", "1b", "2a", "2b", "3a", "3b", "4a", "4b", "5a", "5b" ],
            letter : [ "U1", "U2", "U3", "U4", "U5", "U6", "U7", "U8", "U9", "U10" ],
            rome : [ "i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix", "x" ],
            rome_uppercase : [ "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X" ]
        };

        beforeAll(()=> {
            expect(org.mycore.mets.model.Pagination).toBeDefined();
            expect(org.mycore.mets.model.Pagination.paginationMethods).toBeDefined();
        });

        it("has a list of Pagination Methods", ()=> {
            expect(org.mycore.mets.model.Pagination.paginationMethods.length).toBeGreaterThan(1);
        });

        for (var paginationName_ in paginationExample) {
            var paginationName = paginationName_ + "";
            ((paginationName)=> {
                it("can get can get Pagination Methods by name  (method : " + paginationName + ")",
                    ()=> {
                        expect(org.mycore.mets.model.Pagination.getPaginationMethodByName(paginationName)).toBeDefined()
                    });
            })(paginationName_);
        }

        for (var paginationName_ in paginationExample) {
            ((paginationName)=> {
                it("can parse numbers of the specific pagination method (method : " + paginationName + ")", ()=> {
                    var numberExamples = paginationExample[ paginationName ];
                    var method = org.mycore.mets.model.Pagination.getPaginationMethodByName(paginationName);

                    for (var i = 1; i <= 10; i++) {
                        var numberExample = numberExamples[ i - 1 ];
                        var arabicPageNumber = method.getArabicPageNumber(numberExample);
                        expect(arabicPageNumber).toBe(i);
                    }
                });
            })(paginationName_);
        }


        for (var paginationName_ in paginationExample) {
            ((paginationName)=> {
                it("can convert arabic numbers to numbers of the specific pagination method (method : " + paginationName + ")", ()=> {
                    var method = org.mycore.mets.model.Pagination.getPaginationMethodByName(paginationName);
                    var numberExamples = paginationExample[ paginationName ];

                    for (var i = 1; i <= 10; i++) {
                        expect(method.paginate(i)).toBe(numberExamples[ i - 1 ]);
                    }
                });
            })(paginationName_);
        }

    });
}
