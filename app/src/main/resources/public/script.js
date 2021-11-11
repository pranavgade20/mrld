const getData = async (path) => {
    const data = await (await fetch(path)).json();
    return data;
}

const setAction = (form) => {
    const fileName = document.getElementById('fileName');
    const myForm = document.getElementById("uploadFile");
    const redirect = document.getElementById("redirect");
    myForm.action = window.location.href.replace("/app/", "/upload/") + fileName.value.replace("C:\\fakepath\\", '');
    redirect.setAttribute("href", window.location.href.replace("/upload/", "/app/") + window.location.href.replace(fileName.value.replace("C:\\fakepath\\", ''), ''))
}

const main = async (url) => {
    const data = await getData(url);
    const files = data.files.sort((a, b) => a.name < b.name ? -1 : 1);
    const directories = data.directories.sort();
    const template = document.getElementById('list-item-template');
    const dir_template = document.getElementById('list-item-template-dir');
    const video_template = document.getElementById('list-item-template-video');
    const fileList = document.getElementById('file-list');
    const dirList = document.getElementById('dir-list');
    const fileHead = document.getElementById('file-head');
    const dirHead = document.getElementById('dir-head');
    const historyDisplay = document.getElementById('history');

    historyDisplay.innerText = '/ ' + decodeURI(url).split('listFiles/')[1].replace(/\//g, " / ");

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
            let name = files[i].name;
            let type = files[i].type;
            let item;
            if (type.startsWith("video/")) {
                item = video_template.content.cloneNode(true);
            } else item = template.content.cloneNode(true);
            item.querySelector('.link').innerText = name;
            item.querySelector('.link').setAttribute("href", window.location.href.replace("/app/", "/file/") + name);
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
};


(async () => await main(window.location.href.replace("/app/", "/listFiles/")))();

