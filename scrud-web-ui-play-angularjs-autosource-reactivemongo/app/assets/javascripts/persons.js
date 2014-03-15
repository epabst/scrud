
var app =
  // injects ngResource
  angular.module("app", ["ngResource"])
  // creates the Person factory backed by our autosource
  // Please remark the url person/:id which will use transparently our CRUD AutoSource endpoints
  .factory('Person', ["$resource", function($resource){
    return $resource('persons/:id', { "id" : "@id" });
  }])
  // creates a controller
  .controller("PersonCtrl", ["$scope", "Person", function($scope, Person) {

    $scope.createForm = {};

    // retrieves all persons
    $scope.persons = Person.query();

    // creates a person using createForm and refreshes list
    $scope.create = function() {
      var person = new Person({name: $scope.createForm.name, age: $scope.createForm.age});
      person.$save(function(){
        $scope.createForm = {};
        $scope.persons = Person.query();
      })
    }

    // removes a person and refreshes list
    $scope.remove = function(person) {
      person.$remove(function() {
        $scope.persons = Person.query();
      })
    }

    // updates a person and refreshes list
    $scope.update = function(person) {
      person.$save(function() {
        $scope.persons = Person.query();
      })
    }
}]);