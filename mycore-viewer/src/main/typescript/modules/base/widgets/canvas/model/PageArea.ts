/// <reference path="../../../Utils.ts" />
/// <reference path="../../../components/model/AbstractPage.ts" />
/// <reference path="../viewport/Viewport.ts" />

module mycore.viewer.widgets.canvas {

    export class PageArea implements ViewerPropertyObserver<any> {


        private _pages:Array<model.AbstractPage> = new Array<model.AbstractPage>();
        private _pageAreaInformationMap:MyCoReMap<model.AbstractPage, PageAreaInformation> = new MyCoReMap<model.AbstractPage, PageAreaInformation>();
        private _updateCallback:()=>void = null;

        public set updateCallback(callback:()=>void) {
            this._updateCallback = callback;
        }

        public get updateCallback():()=>void {
            if (this._updateCallback == null) {
                return () => {
                };
            }
            return  this._updateCallback;
        }

        public addPage(page:model.AbstractPage, info:PageAreaInformation):void {
            this.setPageAreaInformation(page, info);
            this._pages.push(page);
            this.registerPageAreaInformationEvents(info);
            this.updateCallback();
        }

        public removePage(page:model.AbstractPage):void {
            this._pages.splice(this._pages.indexOf(page), 1);
            var pageInformation = this._pageAreaInformationMap.get(page);
            this._pageAreaInformationMap.remove(page);
            this.unregisterPageAreaInformationEvents(pageInformation);
            this.updateCallback();
        }

        public propertyChanged(_old:ViewerProperty<any>, _new:ViewerProperty<any>) {
            this._updateCallback();
        }

        public setPageAreaInformation(page:model.AbstractPage, info:PageAreaInformation):void {
            this._pageAreaInformationMap.set(page, info);
        }

        public getPages():Array<model.AbstractPage> {
            return this._pages;
        }

        public getPagesInViewport(viewPort:Viewport) {
            var pages = this._pages;
            var pagesInViewport = new Array<model.AbstractPage>();
            pages.forEach((page) => {
                if (this.pageIntersectViewport(page, viewPort)) {
                    pagesInViewport.push(page);
                }
            });
            return pagesInViewport;
        }

        public getPageInformation(page:model.AbstractPage) {
            return this._pageAreaInformationMap.get(page);
        }

        private pageIntersectViewport(page:model.AbstractPage, viewPort:Viewport) {
            var areaInformation = this._pageAreaInformationMap.get(page);
            var pageDimension = page.size.getRotated(areaInformation.rotation).scale(areaInformation.scale);
            var upperLeftPosition = new Position2D(areaInformation.position.x - pageDimension.width / 2, areaInformation.position.y - pageDimension.height / 2);

            // this is the real rectangle of the page. With upper left corner and the size of the page[rotated])
            var pageRect = new Rect(upperLeftPosition, pageDimension);
            var viewPortRect = viewPort.asRectInArea();

            return pageRect.getIntersection(viewPortRect) != null;
        }


        private registerPageAreaInformationEvents(info:PageAreaInformation) {
            info.positionProperty.addObserver(this);
            info.rotationProperty.addObserver(this);
            info.scaleProperty.addObserver(this);
        }

        private unregisterPageAreaInformationEvents(info:PageAreaInformation) {
            info.positionProperty.removeObserver(this);
            info.rotationProperty.removeObserver(this);
            info.scaleProperty.removeObserver(this);
        }



    }

    export class PageAreaInformation {

        private _positionProperty:ViewerProperty<Position2D> = new ViewerProperty(this, "position", new Position2D(0, 0));
        private _scaleProperty:ViewerProperty<number> = new ViewerProperty(this, "scale", 1);
        private _rotationProperty:ViewerProperty<number> = new ViewerProperty(this, "rotation", 0);

        get positionProperty():ViewerProperty<Position2D> {
            return this._positionProperty;
        }

        get rotationProperty():ViewerProperty<number> {
            return this._rotationProperty;
        }

        get scaleProperty():ViewerProperty<number> {
            return this._scaleProperty;
        }

        get rotation():number {
            return this._rotationProperty.value;
        }

        set rotation(value:number) {
            this.rotationProperty.value = value;
        }

        get scale():number {
            return this.scaleProperty.value;
        }

        set scale(value:number) {
            this.scaleProperty.value = value;
        }

        get position():Position2D {
            return this.positionProperty.value;
        }

        set position(value:Position2D) {
            this.positionProperty.value = value;
        }
    }


}