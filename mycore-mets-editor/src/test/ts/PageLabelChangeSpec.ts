///<reference path="../../../typings/jasmine/jasmine.d.ts"/>
///<reference path="TestUtils.ts"/>
module org.mycore.mets.tests {
    describe("PageLabelChange", ()=> {


        var model:org.mycore.mets.model.MetsEditorModel;
        var simpleChange:org.mycore.mets.model.state.PageLabelChange;

        var newLabel = "new_page_name";
        var oldLabel;
        var emptyMessages = {};

        beforeEach(()=> {
            model = TestUtils.createDefaultModel();
            simpleChange = new org.mycore.mets.model.state.PageLabelChange(model.metsModel.metsPageList[ 0 ], newLabel);
            oldLabel = model.metsModel.metsPageList[ 0 ].orderLabel;
            expect(oldLabel).toBeDefined();
        });

        it("can be executed", ()=> {
            model.stateEngine.changeModel(simpleChange);
            expect(model.metsModel.metsPageList[ 0 ].orderLabel).toBe(newLabel);

        });

        it("can be executed and reverted", ()=> {
            model.stateEngine.changeModel(simpleChange);
            expect(model.metsModel.metsPageList[ 0 ].orderLabel).toBe(newLabel);
            expect(model.stateEngine.canBack).toBeTruthy();
            model.stateEngine.back();
            expect(model.metsModel.metsPageList[ 0 ].orderLabel).toBe(oldLabel);
        });

        it("has a description", ()=> {
            var changeDescription = TestUtils.getWords(simpleChange.getDescription(emptyMessages));
            expect(changeDescription).toContain(newLabel);
            expect(changeDescription).toContain(oldLabel);
        });

    });
}
