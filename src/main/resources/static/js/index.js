$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

	// 获取标记和内容
	var title = $('#recipient-name').val();
	var content = $('#message-text').val();
	// 发送Ajax异步请求
	$.post(
		CONTEXT_PATH + "/discuss/add",
		{"title":title, "content":content},
		function (data) {
			data = $.parseJSON(data);
			// 在提示框返回信息
			$("#hintBody").text(data.msg);
			// 显示提示框
			$("#hintModal").modal("show");
			// 2s后自动隐藏
			setTimeout(function(){
				$("#hintModal").modal("hide");
				// 添加成功后，刷新页面
				if(data.code == 200) {
					window.location.reload();
				}
			}, 2000);
		}
	);

}