# 2ndPro
2020/05/03:目前想法是五個裝置有五個按鈕在畫面上，分別是讓php的index=1~5=>要求不同的數據+時間，前提是五個個別show資料。若有選時段區間(先看AS怎抓)，
  旁邊會有按鈕按下後，抓原本的index(全域static)+time(格式:WHERE BETWEEN T1:固定-7天 AND T2:固定當天，T1、T2是使用者選的)給Get上去，再抓畫面上的值，
  給模型(再裡面預設全NULL，先試試看)用，最後把time=NULL PS:index是按不同按鈕時先=0。
