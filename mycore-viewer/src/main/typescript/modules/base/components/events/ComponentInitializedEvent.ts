/// <reference path="MyCoReImageViewerEvent.ts" />
/// <reference path="../ViewerComponent.ts" />

module mycore.viewer.components.events {
    export class ComponentInitializedEvent extends MyCoReImageViewerEvent {
        constructor(component:ViewerComponent) {
            super(component, ComponentInitializedEvent.TYPE);
        }

        public static TYPE:string = "ComponentInitializedEvent";

    }

}