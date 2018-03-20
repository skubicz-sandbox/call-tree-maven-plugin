Vue.component('method', {
    props: ['method'],
    template:
    '<div class="method-details">' +
    '<div class="comment" v-html="method.comment"></div>' +
    '<div class="annotation" v-for="(annotation) in method.annotations">@{{ annotation.namePart }}</div>' +
    '<div><span v-if="this.method.returnType">{{ this.method.returnType.namePart }}</span> <span class="method-name">{{ this.method.name }}</span>({{ this.drawParameters(this.method.parameters) }})</div>' +
    '</div>',
    methods: {
        drawParameters(parameters) {
            if(!parameters) {
                return "";
            }
            var parametersText = "";
            parameters.forEach(param => {
                parametersText = parametersText + param.namePart + ",";
            })
            return parametersText.slice(0, -1);
        }
    }
})

Vue.component('calls', {
    props: ['current-class-id', 'current-method-root-id', 'calls'],
    //data: function(){
    //    return {
    //    }
    //},
    compiled: function () {
    },
    template:
    '<div class="calls-list">' +
    '   <ul>' +
    '        <li class="" v-for="(call, index) in filterCalls(calls)">' +
    '           <div v-on:click="showMethodDetails" :class="!call.method ? \'empty-method\' : \'\' " class="call-expression" v-if="call.expression">{{ call.expression }}' +
    '              <div class="call-method" style="display: none" v-if="call.method">' +
    '                  <button type="button" v-on:click="closeMethodDetails" class="close" aria-label="Close">&times</button>' +
    '                 <method v-bind:method="call.method"></method>' +
    '             </div>' +
    '           </div>' +
    '           <div class="call-expression" v-if="!call.expression && call.method">' +
    '               <method v-bind:method="call.method"></method>' +
    '           </div>' +
    '           <div class="call-expression" v-if="!call.expression && !call.method">' +
    '               ERROR' +
    '           </div>' +
    '           <calls v-bind:calls="call.children" v-bind:current-method-root-id="currentMethodRootId" v-bind:currentClassId="currentClassId"></calls>' +
    '       </li>' +
    '   </ul>' +
    '</div>',
    methods: {
        filterCalls(calls) {
            return this.$root.filterCalls(calls, this.$root.searchedExpression[this.currentClassId])
        },
        generateId(currentMethodRootId, index) {
            return "expression" + currentMethodRootId + '-' + index;
        },
        showMethodDetails(event) {
            //   $(".execution-method").hide();
            var el = $(event.target).find(".call-method");

            el.toggle();
            //   alert($(event.target).parent().find(".execution-method").attr('class'));
        },
        closeMethodDetails(event) {
            //   $(".execution-method").hide();
            var el = $(event.target).parent();

            el.hide();
            //   alert($(event.target).parent().find(".execution-method").attr('class'));
        }
    }
})

var app = new Vue({
    el: '#application',
    data: {
        classes: DATA.callers,
        filteredClasses: DATA.callers,
        currentClass: null,
        currentClassIndex: 0,
        searchedExpression: [],
        searchedClass: '',
        searchedClassesSize: 0
    },
    methods: {
        showAllMethods() {
            $('.collapse').collapse('show');
        },
        showHideMethod(index) {
            $('#methodRootDetails' + index).collapse('toggle');
        },
        showDetails(index) {
            this.currentClass = this.filteredClasses[index];
            this.currentClassIndex = index;
            $('.collapse').collapse('hide');
        },
        isStringInclude(str, substring) {
            if (!substring) {
                return true;
            }
            if (!str) {
                return false;
            }
            return str.toLowerCase().indexOf(substring.toLowerCase()) > -1;
        },
        isSearchedClassInclude(str) {
            if (!str) {
                return false;
            }
            return this.isStringInclude(str, this.searchedClass);
        },
        filterClasses(classes) {
            var filtered = classes.filter(item => {
                return this.isSearchedClassInclude(item.className.packagePart)
                    || this.isSearchedClassInclude(item.className.namePart)
                //    || this.filterRootMethods(item.methods)
            })

            this.searchedClassesSize = filtered.length;
            this.filteredClasses = filtered;
            return filtered;
        },
        //filterRootMethods(rootMethods) {
        //    if (!rootMethods) {
        //        return false;
        //    }
        //    return rootMethods.filter(item => {
        //        return this.filterMethod(item.method)
        //            || this.filterExecutions(item.executions)
        //    })
        //},
        //isSearchedExecutionInclude(str, searchedExpression) {
        //    if (!str) {
        //        return false;
        //    }
        //    return this.isStringInclude(str, searchedExpression);
        //},
        //filterMethod(method) {
        //    if (!method) {
        //        return false;
        //    }
        //    return this.isSearchedExecutionInclude(method.comment)
        //        || this.isSearchedExecutionInclude(method.name);
        //},
        filterCalls(calls, searchedExpression) {
            if (!calls) {
                return null;
            }
            return calls.filter(item => {
                //   console.log(item.expression);
                return this.filterCall(item, searchedExpression)
            })
        },
        filterCall(call, searchedExpression) {
            if (!call) {
                return false;
            }

            var fit = this.isStringInclude(call.expression, searchedExpression)
                //    || this.filterMethod(item.method)
            ;

            if (fit) {
                return true;
            }
            if (!call.children) {
                return false;
            }

            call.children.forEach(item => {
                if (this.filterCall(item, searchedExpression)) {
                    fit = true;
                }
            })

            return fit;
        }
    }
})