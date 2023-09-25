/**
 * Create the module.
 */

var ksModule = angular.module('%%appname%%', []);

/**
 * Define any configs or statics
 */
//ksModule.config(function ($httpProvider) {
//    $httpProvider.defaults.xsrfCookieName = "CSRF-TOKEN";
//});


/**
 * Controller for the Kitchen Sink plugin.
 */
ksModule.controller('%%appname%%Controller', [ '$scope', function($scope) {
	$scope.welcomeMessage="Hello, World!";
}]

);
