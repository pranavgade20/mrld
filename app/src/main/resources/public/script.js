const getData = async (path) => {
    const data = await (await fetch(path)).json();
    return data;
}

const main = async (url) => {
    const data = await getData(url);
    const path = data.path;
    const files = data.files;
    const directories = data.directories;
    const template = document.getElementById('list-item-template');
    const fileList = document.getElementById('file-list');
    const dirList = document.getElementById('dir-list');

    const displayList = () => {
        fileList.innerHTML = "";
        dirList.innerHTML = "";
        for(let i = 0; i < files.length; i++) {
            const item = template.content.cloneNode(true);
            item.querySelector('.link').innerText = files[i];
            item.querySelector('.link').setAttribute("href", window.location.href + 'file/' + path + '/' + files[i]);
            fileList.append(item);
        }
        for(let i = 0; i < directories.length; i++) {
            const item = template.content.cloneNode(true);
            item.querySelector('.link').innerText = directories[i];
            item.querySelector(".link").addEventListener("click", (e) => {
                main(window.location.href + 'listFiles/' + path + '/' + directories[i]);
            });
            dirList.append(item);
        }
    }
    displayList();
};


(async () => await main(window.location.href + "listFiles/"))();

