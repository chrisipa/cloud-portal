<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<div id="myModal" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" style="display: none;">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button id="close" type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                <button id="auto-scroll" type="button" class="btn btn-danger btn-xs">Auto Scroll</button>
                <h4 class="modal-title" id="myModalLabel">Output</h4>
            </div>
            <div class="modal-body">
                <iframe name="output" id="output" frameborder="0" scrolling="yes"></iframe>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>