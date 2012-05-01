//Codemirror editor
var inputEditor
var outputEditor

function sendData(url, data) {
    $.ajax({
        type:"POST",
        url:url,
        data:data,
//        dataType:"json",
        timeout:30000, //In millis
        beforeSend:function () {
            $('#ajaxSpinner').show();
        },
        /* error: function() {
         $('#status').text('Update failed—try again.').slideDown('slow');
         },*/
        complete:function () {
            $('#ajaxSpinner').hide();
        },
        success:function (data) {
            renderData(data)
        }
    });
}

function renderData(data){
    $('#code-output').show();
    var dataText = data.replace(/\r\n/g,'\n')
//    $('#result').text(dataText);
    outputEditor.setValue(dataText)
}


function setUpCodeMirror() {
    CodeMirror.modeURL = pluginRoot + "/res/ui/codemirror/mode/%N/%N.js";
    inputEditor = CodeMirror.fromTextArea(document.getElementById("code"), {
        lineNumbers:true
    });
    outputEditor = CodeMirror.fromTextArea(document.getElementById("result"), {
            lineNumbers:true
        });
    inputEditor.setOption("mode", "groovy");
    CodeMirror.autoLoadMode(inputEditor, "groovy");
}

$(document).ready(function () {
    $("#executeButton").click(function () {
        inputEditor.save() //Copy the contents to textarea
        sendData(pluginRoot, $("#consoleForm").serialize());
    });

    $('#ajaxSpinner').hide();
    $('#code-output').hide();
    setUpCodeMirror();
});
