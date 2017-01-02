///<reference path="../../../typings/jasmine/jasmine.d.ts"/>

module org.mycore.mets.tests {

    describe("SectionDeleteChange", ()=> {

        var model:org.mycore.mets.model.MetsEditorModel;
        var simpleChange:org.mycore.mets.model.state.SectionDeleteChange;
        var elementToDelete:org.mycore.mets.model.simple.MCRMetsSection;

        var emptyMessages = {};

        beforeEach(()=> {
            model = TestUtils.createDefaultModel();
            elementToDelete = model.metsModel.rootSection.metsSectionList[ 0 ];
            simpleChange = new org.mycore.mets.model.state.SectionDeleteChange(elementToDelete);
        });

        it("can be executed", ()=> {
            expect(model.metsModel.rootSection.metsSectionList[ 0 ]).toBe(elementToDelete);
            model.stateEngine.changeModel(simpleChange);
            expect(model.metsModel.rootSection.metsSectionList[ 0 ]).not.toBe(elementToDelete);
        });

        it("can be executed and reverted", ()=> {
            expect(model.metsModel.rootSection.metsSectionList[ 0 ]).toBe(elementToDelete);
            model.stateEngine.changeModel(simpleChange);
            expect(model.metsModel.rootSection.metsSectionList[ 0 ]).not.toBe(elementToDelete);
            model.stateEngine.back();
            expect(model.metsModel.rootSection.metsSectionList[ 0 ]).toBe(elementToDelete);
        });

        it("has a description", ()=> {
            var description = TestUtils.getWords(simpleChange.getDescription(emptyMessages));
            expect(description).toContain(elementToDelete.label);
            expect(description).toContain(model.metsModel.rootSection.label);
        });
    });
}
