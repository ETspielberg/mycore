/// <reference path="../../components/model/AbstractPage.ts" />

module mycore.viewer.widgets.canvas {
    export class PageLayout {

        public init(model:OrderedListModel, pageController:PageController, pageDimension:Size2D, horizontalScrollbar:Scrollbar, verticalScrollbar:Scrollbar, pageLoader:(number) => void) {
            this._model = model;
            this._pageController = pageController;
            this._pageDimension = pageDimension;
            this._originalPageDimension = pageDimension;
            this._horizontalScrollbar = horizontalScrollbar;
            this._verticalScrollbar = verticalScrollbar;
            this._pageLoader = pageLoader;
        }

        public _originalPageDimension:Size2D;
        public _model:OrderedListModel;
        public _pageController:PageController;
        public _pageDimension:Size2D;
        public _horizontalScrollbar:Scrollbar;
        public _verticalScrollbar:Scrollbar;
        public _pageLoader:(number) => void;

        public _insertedPages:Array<number> = new Array();

        /**
         * Default implementation to update a page.
         * @param order the order number of the page.
         */
        public updatePage(order) {
            var shouldBeInserted = this.checkShouldBeInserted(order);
            var isInserted = this.isImageInserted(order);

            if (shouldBeInserted && !isInserted && this._model.children.has(order)) {
                var page = this._model.children.get(order);
                this._insertedPages.push(order);
                this._pageController.addPage(page, this.calculatePageAreaInformation(order));
            }

            if (!shouldBeInserted && isInserted && this._model.children.has(order)) {
                var page = this._model.children.get(order);
                this._insertedPages.splice(this._insertedPages.indexOf(order));
                this._pageController.removePage(page);
            }

        }

        public getRealPageDimension(pageNumber:number):Size2D {
            if (pageNumber != -1 && this._model.children.has(pageNumber)) {
                var page = this._model.children.get(pageNumber);

                var pageArea = this._pageController.getPageAreaInformation(page);
                if (typeof pageArea != "undefined") {
                    return page.size.scale(pageArea.scale).getRotated(this.getCurrentPageRotation());
                }
            }
            return this._pageDimension.getRotated(this.getCurrentPageRotation());
        }

        /**
         * Should update all needed pages!
         */
        public syncronizePages():void {
            throw "should be implemented";
        }


        /**
         * Should set the PageController to a clear state for a other PageLayout
         */
        public clear():void {
            throw "should be implemented";
        }

        /**
         * Checks if a page should be inserted.
         * @param order the order of the page to check
         */
        public
        checkShouldBeInserted(order:number):Boolean {
            throw "should be implemented";
        }

        /**
         * Should calculate the PageAreaInformation for a specific page
         * @param order the order of the page
         */
        public calculatePageAreaInformation(order:number):PageAreaInformation {
            throw "should be implemented";
        }

        /**
         *Gets the Position of a image in the specific layout
         * @param order  the order of the page
         */
        public getImageMiddle(order:number):Position2D {
            throw "should be implemented";
        }

        /**
         * Should fit the current page to the screen
         */
        public fitToScreen():void {
            throw "should be implemented";
        }

        /**
         * Should fit the current page to the width of the screen
         */
        public fitToWidth(attop:boolean = false):void {
            throw "should be implemented";
        }

        /**
         * Should return the current page
         */
        public getCurrentPage():number {
            throw "should be implemented";
        }

        /**
         * Should jump to a specific page in the layout
         * @param order  the order of the page
         */
        public jumpToPage(order:number):void {
            throw "should be implemented";
        }

        public isImageInserted(order:number) {
            return this._model.children.has(order) && (this._pageController.getPages().indexOf(this._model.children.get(order)) != -1);
        }

        /**
         * This method will be called if the user scrolls in the scrollbarElement
         */
        public scrollhandler():void {
            throw "should be implemented";
        }

        /**
         * This method will be called if the pages or the viewport should be rotated. The behavior depends on the layout.
         * @param deg the new rotation in degree
         */
        public rotate(deg:number):void {
            throw "should be implemented";
        }

        /**
         * Should return the label key for i18n which should be used to display a label in the Toolbar.
         */
        public getLabelKey():string {
            throw "should be implemented";
        }

        /**
         * Should return the Part of the viewport which should be rendered as the viewport
         */
        public getCurrentOverview():Rect {
            throw "should be implemented";
        }


        public next():void {
            throw "should be implemented";
        }

        public previous():void {
            throw "should be implemented";
        }

        public getCurrentPageRotation():number {
            throw "should be implemented";
        }

        public setCurrentPageZoom(zoom:number):void {
            throw "should be implemented";
        }

        public getCurrentPageZoom():number {
            throw "should be implemented";
        }

        public getCurrentPositionInPage():Position2D {
            throw "should be implemented";
        }

        public setCurrentPositionInPage(pos:Position2D):void {
            throw "should be implemented";
        }
    }

    export interface OrderedListModel {
        children: MyCoReMap<number, model.AbstractPage>;
        pageCount: number;
    }
}