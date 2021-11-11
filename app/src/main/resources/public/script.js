const getData = async (path) => {
    const data = await (await fetch(path)).json();
    return data;
}

const history = [];

const setAction = (form) => {
    const myFile = document.getElementById('uploadFile');
    const onSubmit = document.getElementById('submit')
    const path = data.path;
//    alert(fileName.value.replace("C:\\fakepath\\", ' '));
    form.action = window.location.href.replace("/app/", "/upload/") + path + '/' + fileName.value.replace("C:\\fakepath\\", ' ');
    return false;
}

const main = async (url) => {
    const data = await getData(url);
    const path = data.path;
    const files = data.files.sort();
    const directories = data.directories.sort();
    const template = document.getElementById('list-item-template');
    const dir_template = document.getElementById('list-item-template-dir');
    const fileList = document.getElementById('file-list');
    const dirList = document.getElementById('dir-list');
    const fileHead = document.getElementById('file-head');
    const dirHead = document.getElementById('dir-head');
    const backButton = document.getElementById('back');
    const historyDisplay = document.getElementById('history');

    if (history.length > 0) backButton.style.display = "block";
    else backButton.style.display = "none";

    historyDisplay.innerText = url.split('listFiles/')[1].replace(/\//g, " > ");

    const displayList = () => {
        fileHead.style.display = "none";
        dirHead.style.display = "none";
        fileList.innerHTML = "";
        dirList.innerHTML = "";
        for (let i = 0; i < files.length; i++) {
            if (i === 0) {
                fileHead.style.display = "block";
                dirHead.style.display = "block";
            }
            const item = template.content.cloneNode(true);
            item.querySelector('.link').innerText = files[i];
            item.querySelector('.link').setAttribute("href", window.location.href.replace("/app/", "/file/") + files[i]);
            fileList.append(item);
        }

        for (let i = 0; i < directories.length; i++) {
            const item = dir_template.content.cloneNode(true);
            item.querySelector('.link').innerText = directories[i];
            item.querySelector('.link').setAttribute("href", window.location.href + directories[i] + '/');
            dirList.append(item);
        }
    }
    displayList();

    backButton.addEventListener("click", e => main(history.pop()));
};


(async () => await main(window.location.href.replace("/app/", "/listFiles/")))();

