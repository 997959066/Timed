ip='localhost'; // dev xiaoyu
//ip='10.88.27.116'; // sit 
//ip='10.73.16.51'; // uat





window.api = 'http://'+ip+':18080/'; 
$('#aIp').attr('href',window.api+'list.html'); 
window.handleH = getHandleBoxHeight();
Date.prototype.Format = function (fmt) { //author: meizz 
  var o = {
      "M+": this.getMonth() + 1, //月份 
      "d+": this.getDate(), //日 
      "h+": this.getHours(), //小时 
      "m+": this.getMinutes(), //分 
      "s+": this.getSeconds(), //秒 
      "q+": Math.floor((this.getMonth() + 3) / 3), //季度 
      "S": this.getMilliseconds() //毫秒 
  };
  if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
  for (var k in o)
  if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
  return fmt;
}

/ 
//判断是否为json
function isJsonString(str) {  
  try {  
    if (typeof JSON.parse(str) == "object") {  
      return true;  
    }  
  } 
  catch(e) {  
  }  
  return false;  
}  

// 获取操作框的高度
function getHandleBoxHeight() {
  var h = $('#handleBox').outerHeight() + 40 + 34;
  return h;
}
