

function addTable(tableId, headings, table_data) {

    var table = document.getElementById(tableId)

    //Create TABLE headings
    var tableHead = document.createElement('thead')
    table.appendChild(tableHead);
    var tr = document.createElement('TR');
    tableHead.appendChild(tr);
    for (i = 0; i < headings.length; i++) {
        var th = document.createElement('TH')
//      th.width = '75';
        th.appendChild(document.createTextNode(heading[i]));
        tr.appendChild(th);

    }

    //TABLE Body
    var tableBody = document.createElement('tbody')
    table.appendChild(tableBody);

    for (i = 0; i < table_data.length; i++) {
        var tr = document.createElement('tr');
        tableBody.appendChild(tr);
        for (j = 0; j < table_data[i].length; j++) {
            var td = document.createElement('td')
            td.appendChild(document.createTextNode(stock[i][j]));
            tr.appendChild(td)
        }
    }

}

function quickTree(elemId, tree_data, func) {

    var tree = document.getElementById(elemId);
    if(tree_data){
        var treeRoot = document.createElement('ul');
//        treeRoot.className += "nav";
//        treeRoot.className += "nav-list";
//        treeRoot.setAttribute('data-toggle', 'collapse');
        tree.appendChild(treeRoot);
        createTree(treeRoot, tree_data, 0)
    }

    function createTree(rootElem, data, depth){

        if(data){
            var treeNode = document.createElement('li')
            rootElem.appendChild(treeNode);
            var style = document.createElement('a');
//            style.className += "tree-toggle";
//            style.className += "nav-header";
            treeNode.appendChild(style)
            style.appendChild(document.createTextNode(data.name))
            treeNode.onClick = function(e){
              alert("clicked");
              nodeOnClick(e, depth);
            }
            if(data.children){
//                treeNode.className += "open";
                for(var i=0; i<data.children.length; i++){
                    var listedChild = document.createElement('ul')
                    treeNode.appendChild(listedChild)
//                    listedChild.setAttribute('data-toggle', 'collapse')
//                    listedChild.className += "nav";
//                    listedChild.className += "nav-list";
//                    listedChild.className += "tree";
                    child = data.children[i]
//                    listedChild.appendChild(document.createTextNode(child.name))
                    createTree(listedChild, child, depth + 1)
                }
            }
        }
    }

    function nodeOnClick(event, depth){
        var target = getEventTarget(event);
        alert(target.innerHTML);
        alert(depth);
//        func(target, depth);
    }


}




