const data = {
    "path": "priyanshu/Desktop",
    "files": [
      ".directory",
      "Mcgraw-Hill - Option Pricing And Volatility - Advanced Strategies And Trading Techniques - Sheldon Natenberg - (1994).pdf",
      "Handbook of Technical Analysis - The Practitioner's Comprehensive Guide to Technical Analysis (2015).pdf",
      "Computer Networks A Top Down Approach.pdf",
      "Computer Architecture Computer Organization And Design.pdf",
      "Day-Trading-Stocks-Wall-Street-Way-Josh-Dipietro.pdf",
      "Theory of Automata.pdf",
      "Encyclopedia-of-Chart-Patterns.pdf",
      "movie.mp4"
    ],
    "directories": [
      "CFA",
      "Data Structure & Algorithm using C++",
      "Probability & Statistics Master",
      "GMAT Official Guides 2019",
      "DU",
      " Linux Training Course",
      "ML Projects",
      "Java Programming Masterclass for Software Developers",
      "Deep Learning A-Z™ Hands-On Artificial Neural Networks",
      "IELTS",
      "Complete SQL + Databases Bootcamp Zero to Mastery [2020]",
      " English Grammar  Master Course  All Levels  All Topics",
      "Machine Learning A-Z™ Hands-On Python & R In Data Science",
      "Pyhton Bootcamp",
      "2021 Microsoft Excel from A-Z Beginner To Expert Course",
      "CAT",
      "Python For Data Science With Real Exercises!"
    ]
  };

const path = data.path;
const files = data.files;
const directories = data.directories;
const template = document.getElementById('list-item-template');
const fileList = document.getElementById('file-list');
const dirList = document.getElementById('dir-list');

displayList = () => {
    for(let i = 0; i < files.length; i++) {
        const item = template.content.cloneNode(true);
        item.querySelector('.link').innerText = files[i];
        item.querySelector('.link').setAttribute("href", window.location.href + 'file' + '/' + path + '/' + files[i]);
        fileList.append(item);
    }

    for(let i = 0; i < directories.length; i++) {
        const item = template.content.cloneNode(true);
        item.querySelector('.link').innerText = directories[i];
        item.querySelector('.link').setAttribute("href", window.location.href + 'listFiles' + '/' + path + '/' + directories[i]);
        dirList.append(item);
    }
}

displayList();
