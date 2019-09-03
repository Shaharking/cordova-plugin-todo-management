
var cordova = require('cordova'),
    exec = require('cordova/exec');

var get = function (success, error) {
    exec(success, error, 'TodoManagement', 'get');
}

var getById = function(success, error, args) {
    exec(success, error, 'TodoManagement', 'getById', args);
}

var post = function (success, error, args) {
    console.log(arguments);
    exec(success, error, 'TodoManagement', 'post', args);
}

var put = function (success, error, args) {
    console.log(arguments);
    exec(success, error, 'TodoManagement', 'put', args);
}

var remove = function (success, error, args) {
    console.log(arguments);
    exec(success, error, 'TodoManagement', 'delete', args);
}

module.exports = { delete: remove, put, get, post, getById }