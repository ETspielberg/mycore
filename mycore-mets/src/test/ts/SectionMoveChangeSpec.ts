///<reference path="../../../typings/jasmine/jasmine.d.ts"/>

module org.mycore.mets.tests {
    describe("SectionMoveChange", ()=> {

        var model:org.mycore.mets.model.MetsEditorModel;
        var simpleChange:org.mycore.mets.model.state.SectionMoveChange;
        var sectionToMove:org.mycore.mets.model.simple.MCRMetsSection;
        var moveTarget:org.mycore.mets.model.DropTarget;
        var moveToIndex = 1;

        var emptyMessages = {};

        beforeEach(()=> {
            model = TestUtils.createDefaultModel();
            sectionToMove = model.metsModel.rootSection.metsSectionList[ 0 ];
        });

        it("can be executed(position=after)", ()=> {
            moveTarget = {position : "after", element : model.metsModel.rootSection.metsSectionList[ moveToIndex ]};
            simpleChange = new org.mycore.mets.model.state.SectionMoveChange(sectionToMove, moveTarget);

            /**
             * [0] -> [1]
             * [1] -> [0]
             * [2] -> [2]
             */
            expect(model.metsModel.rootSection.metsSectionList.indexOf(sectionToMove)).toBe(0);
            model.stateEngine.changeModel(simpleChange);
            expect(model.metsModel.rootSection.metsSectionList.indexOf(sectionToMove)).toBe(1);
        });

        it("can be executed(position=after) and reverted", ()=> {
            moveTarget = {position : "after", element : model.metsModel.rootSection.metsSectionList[ moveToIndex ]};
            simpleChange = new org.mycore.mets.model.state.SectionMoveChange(sectionToMove, moveTarget);

            expect(model.metsModel.rootSection.metsSectionList.indexOf(sectionToMove)).toBe(0);
            model.stateEngine.changeModel(simpleChange);
            expect(model.metsModel.rootSection.metsSectionList.indexOf(sectionToMove)).toBe(1);
            model.stateEngine.back();
            expect(model.metsModel.rootSection.metsSectionList.indexOf(sectionToMove)).toBe(0);
        });

        it("has a description", ()=> {
            moveTarget = {position : "after", element : model.metsModel.rootSection.metsSectionList[ moveToIndex ]};
            simpleChange = new org.mycore.mets.model.state.SectionMoveChange(sectionToMove, moveTarget);

            var changeDescription = TestUtils.getWords(simpleChange.getDescription(emptyMessages));
            expect(changeDescription).toContain(model.metsModel.rootSection.label);
            expect(changeDescription).toContain(moveTarget.position);
            expect(changeDescription).toContain(moveTarget.element.label);

        });
    });
}
