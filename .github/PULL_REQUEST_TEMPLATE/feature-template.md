## Feature description
<!--Clearly and concisely describe the feature.-->
This feature loads old invitations of a server and show the old invitations in the invitations list view of the EditServerScreen.
First invitations are loaded, then you can delete a displayed invitation or copy with double click to the system clipboard

## Solution description
<!--Describe your code changes in detail for reviewers.-->
GenModel
- Add invitations and link invitations to a server

EditServerScreen
- Expand fxml with list view and delete button
- loadInvitation and createLvInvitation
- Add method to copy invitation to system clipboard
- Add functionality to delete invitations

NetworkController
- Add method to load new invitations
- Add method to delete new invitations

RestClient
- Add request to load new invitations
- Add request to delete invitation

JsonUtil
- Add method do parse new invitations

ServerScreen
- Handle inviteExpired

fix:
- Add StyleClass to invitationAmount

## Areas affected and ensured
<!--List out the areas affected by your code changes.--> 

- GenModel
- EditServerScreen
- NetworkController
- RestClient
- JsonUtil
- ServerScreen
- StyleClasses

## Is there any existing behavior change of other features due to this code change?
<!--Mention Yes or No. If Yes, provide the appropriate explanation.-->

No, there are no behavior changes of other features

## Test cases
<!--Have you tested implemented test cases for this feature?-->

This feature is tested in the EditServerScreenControllerTest
 - loadInvitationsFailureTest()
 - loadAndDeleteInvitationsSuccessfulTest()
 - deleteInvitationsFailureTest()