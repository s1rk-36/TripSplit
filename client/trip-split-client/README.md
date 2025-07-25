- npm install
- npm install react-router-dom
- npm install react-icons
- npm install react-bootstrap

# TripSplit

- Users
  - [x] CRUD
    - [x] All Users are displayed when logging as Admin
    - [x] Users can be Added By Registering
    - [x] Users can be Updated through Account Setting
    - [x] After updating email, trying to log in with the old email doens't work
    - [x] After updating email. loggin in with new email works
    - [x] Admin can Delete Users
  - [x] 2 Roles: USER, ADMIN
- Groups
  - [x] CRUD
    - [x] All Groups are displayed when logging as Admin
    - [x] All Groups a user belongs to can be seen when logged in as said user
    - [x] Anyone can see the details of the group like members of the group
    - [x] Groups can be added by any users
    - [x] Groups can be edited by the group admin
    - [x] The ADMIN role can edit any group
    - [x] The ADMIN role can delete any group
  - [x] Users can join any existing group through their Id
    - [x] If select non-existing groupId an error message appears
    - [x] If already part of the group and try to join again an error message appears
  - [x] Only group admin can kick people out of the group
  - [x] Invite Code can be copied from the button
- Expenses
  - [x] CRUD
    - [x] When entering a group all expenses from that group are displayed
    - [x] Anyone can add a new expense to the group
    - [x] Expenses can be updated by their creator
    - [x] Expenses can be deleted by their creator
  - [x] name validation 3 > and < 100 works
  - [x] totalCost must be positive number
  - [x] Category and group are required to be selected
  - [x] Date must be in the future
  - [x] Amount can be split between members equally
  - [x] Amount can be split with custom values 
  - [x] Search Bar works
  - [ ] **Group dropdown works allowing you to change and filter which expenses you want to see**
  - [x] Filter By Category
  - [x] Time dropdown allowing you to filter to Today, this week or this month
  - [x] Filter Alphabetically, By amount and by newest or oldest
- Receipts
  - [ ] CRUD (missing update - but I it is fine like that)
    - [ ] When going to group details all receipts from that expense are displayed
    - [x] When creating an expense users can Add a receipt
    - [x] When editing a group users can Add more receipts
    - [x] When editing a group users can Delete a receipt

 # Bugs Found

### Can't see receipts from the DB
- [ ] When going to group details all receipts from that expense are displayed
  - I can see the ones I added but not the ones already added from the database

### Group Dropdown
- [ ] **Group dropdown works allowing you to change and filter which expenses you want to see**
- The group dropdown works and lets you choose other groups but the expenses displayed are tied to the current group.
- Selecting all expenses shows you all expenses in that group but not expenses from other groups.
- Selecting another group from the one you are currently in won't display anything 
- To see the expenses of that group you have to actually enter the group.

### Weird Bug with uploading receipts (Happened only once, Don't know the cause)
- I don't know what triggered it but when testing the app I started adding receipts with multiple images and after a while an error appeared.
  - I tried recreating it by adding multiple receipts to the same group but the error didn't appeared again.
  - It let me add over 10+ receipts to the same group without issue.

### validatePassword method causes bug (commented the method out for now)
- There is a bug if you uncomment the validatePassword method in user Service.
- I uncommented it to fix a test, but after seeing the bug I commented the method again
- If validatePassword uncommented, then after updating it makes a this message pop up "password must be at least 8 characters" even if the password is longer than 8 characters