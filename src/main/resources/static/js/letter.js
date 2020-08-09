$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide");

	var toName = $("#recipient-name").val();
	var toContent = $("#message-text").val();
	// 发送ajax
	$.post(
		CONTEXT_PATH + "/letter/send",
		{"toName":toName, "toContent":toContent},
		function (data) {
			data = $.parseJSON(data);
			$("#hintBody").text(data.msg);
			// 显示提示框
			$("#hintModal").modal("show");
			// 重新刷新
			setTimeout(function(){
				$("#hintModal").modal("hide");
				location.reload();

			}, 2000);
		}
	);

}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}