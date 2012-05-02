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
}

function updateWithOption(opt){
    setLangMode(inputEditor,opt.attr('langMode'))
    $('[name=lang]').val(opt.val())
}

function setLangMode(editor, modeName) {
    if(!modeName){
        modeName = "text/plain"
    }else{
        CodeMirror.autoLoadMode(inputEditor, modeName);
    }
    editor.setOption("mode", modeName);
}

function setUpLangOptions() {
    var codeLang = $('#codeLang')
    var options = codeLang.attr('options');
    codeLang.empty()

    for(var index in scriptConfig){
        var config = scriptConfig[index]
        var opt = new Option(config.langName,config.langCode);
        if(config.mode){
            opt.langMode = config.mode;
        }
        options[options.length] = opt
    };
    $('#codeLang').change(function(){
        var opt = $(this).find(":selected");
        updateWithOption(opt)
    });

    $('#codeLang option:eq(0)').attr('selected','selected')
    updateWithOption($(options[0]))
}

$(document).ready(function () {
    $("#executeButton").click(function () {
        inputEditor.save() //Copy the contents to textarea
        sendData(pluginRoot, $("#consoleForm").serialize());
    });

    $('#ajaxSpinner').hide();
    $('#code-output').hide();
    setUpCodeMirror();
    setUpLangOptions();
});
