//'use strict';
(function() {


	function %%UpperWidgetName%%DirectiveCtrl($q, %%UpperWidgetName%%Service) {

		var me = this;
		this.message=undefined;
		
		this.initialize=function(){
			%%UpperWidgetName%%Service.getContent().then(function(result) {
				console.log("result="+result.data);
				me.message=result.data;
			})
		};
	    
	    this.initialize();
	}
	
	%%UpperWidgetName%%DirectiveCtrl.$inject = ['$q', '%%UpperWidgetName%%Service'];
	var widgetFunction = function() {
	    angular.module('sailpoint.home.desktop.app')
	    .service('%%UpperWidgetName%%Service', ['SP_CONTEXT_PATH', '$http', function(SP_CONTEXT_PATH, $http) {

		    this.getContent = function() {
		        return $http.get(SP_CONTEXT_PATH + '/plugin/rest/%%widgetName%%/message');
		    };
		
		}])
		.controller('%%UpperWidgetName%%DirectiveCtrl', %%UpperWidgetName%%DirectiveCtrl)
		.directive('sp%%UpperWidgetName%%Widget', function() {
			   //console.log("Directive");
			    return {
			        restrict: 'E',
			        scope: {
			            widget: '=spWidget'
			        },
			        controller: '%%UpperWidgetName%%DirectiveCtrl',
			        controllerAs: '%%widgetName%%Ctrl',
			        bindToController: true,

			        template:
			            '<div class="seri-widget" sp-loading-mask="%%widgetName%%Ctrl.message">' +
			            '  <div class="panel-body" >' +
			            '    <div>Widget says {{ %%widgetName%%Ctrl.message }} !!</div>'+
			            '  </div>' +
			            '  <div class="panel-footer">A footer' +
			            '  </div>' +
			            '</div>'
			    };
		});

	};
	PluginHelper.addWidgetFunction(widgetFunction);
})();