contextPath = '/pipeline61';
appModel = 'jetty'; //tomcat or jetty
var role = 'customer';

function serialize($context) {
    return $context.serialize();
}

function ajaxSend(url, method, data, key, redirect, func){
    $.ajax({
        url: url,
        data: data,
        dataType: "json",
        cache: false,
        type: method,
        beforeSend: function(request) {
            request.setRequestHeader("key", role);
        },
        error: function () {
            console.log('failed to get AJAX data .');
        },
        success: function (result) {
//            alert(result);
            func(result);
            if(redirect)
                window.location.href = redirect;
        }
    });
}


function updateOrder(_id, _type, _amount, _additions, _state, redirect){
    var data = { id: _id,
        type: _type,
        amount: _amount,
        additions : _additions,
        state: _state
    };

    var path = "";
    if(appModel == 'tomcat'){
        path = contextPath+'/server/order?id='+_id
            + '&type=' + _type
            + '&amount=' + _amount
            + '&additions=' + _additions
            + '&state=' + _state ;
    } else {
        path = contextPath+'/server/order';
    }
    ajaxSend(path, 'PUT', data, role, redirect);
}

function payOrder(_id, _payType, _amount, _cardInfo, redirect){
    var data = { id: _id,
        type: _payType,
        amount: _amount,
        cardDetails : _cardInfo
    };

    var path = "";
    if(appModel == 'tomcat'){
        path = contextPath+'/server/payment?id='+_id
            + '&type=' + _payType
            + '&amount=' + _amount
            + '&cardDetails=' + _cardInfo;
    } else {
        path = contextPath+'/server/payment';
    }
    ajaxSend(path, 'PUT', data, role, redirect);
}

function cancelOrder(_id){
    var data = { id: _id ,
        key: role};
    ajaxSend(contextPath+'/server/order?id='+_id, 'DELETE', data, '1234', null);
}

function optionOrder(_id){
    var data = { key: role};
    var url = contextPath+'/server/order/options?id='+_id
    $.ajax({
        url: url,
        data: data,
        dataType: "json",
        cache: false,
        type: 'GET',
        beforeSend: function(request) {
            request.setRequestHeader("key", role);
        },
        error: function () {
            console.log('failed to get AJAX data .');
        },
        success: function (result) {
            alert('Operations: ' + result.operations);
        }
    });
}


function getElements(formId) {
    var form = document.getElementById(formId);
    var elements = new Array();
    var tagElements = form.getElementsByTagName('input');
    alert(tagElements.length)
    for (var j = 0; j < tagElements.length; j++){
        elements.push(tagElements[j]);

    }
    return elements;
}

//获取单个input中的【name,value】数组
function inputSelector(element) {
    if (element.checked)
        return [element.name, element.value];
}

function input(element) {
    switch (element.type.toLowerCase()) {
        case 'submit':
        case 'hidden':
        case 'password':
        case 'text':
            return [element.name, element.value];
        case 'checkbox':
        case 'radio':
            return inputSelector(element);
    }
    return false;
}

//组合URL
function serializeElement(element) {
    var method = element.tagName.toLowerCase();
    var parameter = input(element);

    if (parameter) {
        var key = encodeURIComponent(parameter[0]);
        if (key.length == 0) return;

        if (parameter[1].constructor != Array)
            parameter[1] = [parameter[1]];

        var values = parameter[1];
        var results = [];
        for (var i=0; i<values.length; i++) {
            results.push(key + '=' + encodeURIComponent(values[i]));
        }
        return results.join('&');
    }
}

//调用方法
function serializeForm(formId) {
    var elements = getElements(formId);
    var queryComponents = new Array();

    for (var i = 0; i < elements.length; i++) {
        var queryComponent = serializeElement(elements[i]);
        if (queryComponent)
            queryComponents.push(queryComponent);
    }

    return queryComponents.join('&');
}



function loadMonitorData(container, propName, ks) {
    //            params= {prop: propName, 'keys': ks};
    var params = "prop=" + propName;
    for (var i = 0; i < ks.length; i++)
        params += "&keys=" + ks[i];
    $.post("getMonitorData", params, function (data, status) {
        if (data)
            createHighStock(propName, container, ks, data);
    }, "json");
};

function loadDataToContainer(container,propName) {
    params = {prop: propName};
    $.post("getKeys", params, function (data, status) {
        if (data)
            loadMonitorData(container, propName, data);
    }, "json");
};

function loadDataOfProp(container,propName,key) {
    if(propName) {
        params = {prop: propName,key:key};
        $.post("getPropsLike", params, function (data, status) {
            if (data)
                loadDataToContainer(data[0], container);
        }, "json");
    }
}

function loadMatchedData(container, propName, key , duraiton) {
    var params = "prop=" + propName;
    if(key)
        params += "&key=" + key;
    if(duraiton)
        params += "&duration =" + duraiton;
    $.post("getPropKeyLike", params, function (data, status) {
        if (data && data[0] && data[1])
            loadMonitorData(container, data[0], data[1]);
    }, "json");
}

function loadCpuMockData() {
    var dataNames = ['127.0.0.1:10010', '127.0.0.1:10020'];
    var data = [mockdata(), mockdata2()];
    createHighStock("CPUMonitor", '#highstock-cpu', dataNames, data);
}

function loadJvmMockData() {
    var dataNames = ['127.0.0.1:10010', '127.0.0.1:10020'];
    var data = [mockdata(), mockdata2()];
    createHighStock("JVM Monitor", '#highstock-jvm', dataNames, data);
}

function loadMemMockData() {
    var dataNames = ['127.0.0.1:10010', '127.0.0.1:10020'];
    var data = [mockdata(), mockdata2()];
    createHighStock("MemoryMonitor", '#highstock-mem', dataNames, data);
}