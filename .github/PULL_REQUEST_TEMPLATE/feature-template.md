## Feature description
Adds Contextmenu to Servr Screen to leave the server if you are not the owner.

## Solution description
adds a context menu to the server name in the server screen with an item to leave the server.
If you are not the server owner, the contextmenu is shown and when you click on it, the AttentionServerScreen opens.
Creates AttentionLeaveServerScreen.fxml and a controller for it.
when you press cancel button nothing happens and AttentionScreen closes. If you press the Leave button, you leave the server and return to the main screen.
Creates REST request to leave the server.

## Areas affected and ensured
StageManager.java, ServerScreenController.java,
Editor.java, NetworkController.java, RestClient.java

## Covered test cases
Tests all implementations, functionalities and also the REST queries.

## Is there any existing behavior change of other features due to this code change?
No.

## Test cases
Yes.