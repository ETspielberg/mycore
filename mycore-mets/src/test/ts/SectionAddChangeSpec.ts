///<reference path="../../../typings/jasmine/jasmine.d.ts"/>

module org.mycore.mets.tests {

    import MCRMetsSection = org.mycore.mets.model.simple.MCRMetsSection;

    describe("SectionAddChange", ()=> {

        var model:org.mycore.mets.model.MetsEditorModel;
        var simpleChange:org.mycore.mets.model.state.SectionAddChange;
        var newSection:MCRMetsSection;
        var emptyMessages = {};

        beforeEach(()=> {
            model = TestUtils.createDefaultModel();
            newSection = new MCRMetsSection("id", "type", "NewSection");
            simpleChange = new org.mycore.mets.model.state.SectionAddChange(newSection, model.metsModel.rootSection);
        });

        it("can be executed", ()=> {
            model.stateEngine.changeModel(simpleChange);
            expect(newSection.parent).toBe(model.metsModel.rootSection);
            expect(model.metsModel.rootSection.metsSectionList).toContain(newSection);
        });

        it("can be executed and reverted", ()=> {
            model.stateEngine.changeModel(simpleChange);
            expect(newSection.parent).toBe(model.metsModel.rootSection);
            expect(model.metsModel.rootSection.metsSectionList).toContain(newSection);
            expect(model.stateEngine.canBack()).toBeTruthy();
            model.stateEngine.back();
            expect(newSection.parent).not.toBe(model.metsModel.rootSection);
            expect(model.metsModel.rootSection.metsSectionList).not.toContain(newSection);
        });

        it("has a description", ()=> {
            var descriptionWords = TestUtils.getWords(simpleChange.getDescription(emptyMessages));
            expect(descriptionWords).toContain(newSection.label);
            expect(descriptionWords).toContain(model.metsModel.rootSection.label);
        });
    });
}
