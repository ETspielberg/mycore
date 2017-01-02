{
    "use strict";
    angular.module("ng-fixed-scroll-header", []).directive('scrollBox', function () {
        return {
            restrict: 'A',
            link: function (scope, el, attrs, controller) {
                if (typeof el.scrollTop == 'undefined') {
                    throw new Error("ng-fixed-scroll-header requires JQuery!");
                }

                controller.init(el, scope);

                // The element needs to have a fixed height or the script wont work
                el.css({
                    "overflow-y": "scroll"
                });

                el.bind('scroll', function (event) {
                    var scrollPosition = el.scrollTop();
                    controller.scrolled(scrollPosition);
                });
            },
            controller: function () {
                this.childList = [];
                this.lastFixed = null;
                this.element = null;
                this.scope = null;

                this.init = function (el, scope) {
                    this.element = el;
                    this.scope = scope;
                    this.updateChilds();
                };

                this.calculatePosition = function (elem) {
                    return this.element.scrollTop() + (elem.offset().top - this.element.offset().top);
                };

                this.updateChilds = function () {
                    this.clearLastFixed();
                    var that = this;
                    this.childList.forEach(function (child) {
                        child.position = that.calculatePosition(child.selector);
                    });
                    this.scrolled(this.element.scrollTop());
                };

                this.registerHeaderChild = function (elem) {
                    this.childList.push({selector: elem, position: -1});
                    this.updateChilds(elem);
                    this.watchChild(elem);
                };

                this.registerContentChild = function (elem) {
                    this.updateChilds();
                    this.watchChild(elem);
                };

                this.watchChild = function (elem) {
                    var that = this;
                    this.scope.$watch(function () {
                        return elem.height();
                    }, function (oldHeight, newHeight) {
                        that.updateChilds();
                    });

                    this.scope.$watch(function () {
                        return elem.position().top;
                    }, function (oldPosition, newPosition) {
                        that.updateChilds();
                    });
                };

                this.clearLastFixed = function () {
                    if (this.lastFixed != null) {
                        this.lastFixed.selector.css({"top": "", position: "relative"});
                        this.lastFixed = null;
                    }
                };

                this.scrolled = function (scrollPosition) {
                    var MINIMAL_INTEGER = -1 * Math.pow(2, 32);

                    var fixedChild = this.childList.reduce(function (nearest, current) {
                        var offset = current.position - scrollPosition;
                        var lastOffset = (nearest == null) ? MINIMAL_INTEGER : nearest.position - scrollPosition;

                        if (offset > lastOffset && offset <= 0) {
                            return current;
                        } else {
                            return nearest;
                        }
                    }, null);

                    if (this.lastFixed != null && fixedChild != this.lastFixed) {
                        this.clearLastFixed();
                    }

                    if (fixedChild != null) {
                        fixedChild.selector.css({top: scrollPosition - fixedChild.position + "px"});
                        this.lastFixed = fixedChild;
                    }
                };
            }
        }
    });


    angular.module("ng-fixed-scroll-header").directive('fixedHeader', function () {
        return {
            require: '^scrollBox',
            restrict: 'A',
            link: function (scope, elem, attrs, scrollBoxCtrl) {
                elem.css({width: "100%", position: "relative"});
                scrollBoxCtrl.registerHeaderChild(elem);
            }
        };
    });
    angular.module("ng-fixed-scroll-header").directive('scrollContent', function () {
        return {
            require: '^scrollBox',
            restrict: 'A',
            link: function (scope, elem, attrs, scrollBoxCtrl) {
                scrollBoxCtrl.registerContentChild(elem);
            }
        }
    });

}

