/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
var browser = require('cordova/platform');
var winCallBack;

function _get()
{
    let todos = window.localStorage.getItem('todo');
    if (todos) {
        todos = JSON.parse(todos);
    } else {
        todos = [];
    }
    return todos;
}

function _save(todos)
{
    window.localStorage.setItem('todo', JSON.stringify(todos));
}

function get(win, fail, args, env)
{
    const todos = _get();
    win(todos);
}

function getById(win, fail, args, env)
{
    const todo = args[0];
    const id = todo.id;

    const todos = _get();
    const mytodo = todos.find(todo => todo.id === id);
    win(mytodo);
}

function post(win, fail, args, env)
{
    const todo = args[0];
    let todos = _get();
    todos.push(todo);
    _save(todos);
    win();
}

function deleteTodo(win, fail, args, env)
{
    const todo = args[0];
    const id = todo.id;
    let todos =  _get();
    let index = todos.findIndex(x => x.id === id);
    todos.splice(index, 1);
    _save(todos);
    win();
}

function put(win, fail, args, env)
{
    const todo = args[0];
    const id = todo.id;
    let todos =  _get();
    const todoItem = todos.find(x => x.id === id);
    Object.assign(todoItem, args);
    _save(todos);
    win();
}

module.exports = {
    get,
    getById,
    post,
    delete: deleteTodo,
    put
};

require('cordova/exec/proxy').add('TodoManagement', module.exports);
