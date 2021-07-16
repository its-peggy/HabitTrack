Original App Design Project - README Template
===

# Habit Tracker

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)
3. [Further Documentation](#Further-Documentation)

## Overview
### Description
App that lets you create and categorize daily habits (e.g. drink 8 cups of water, morning meditation), check off completed habits, and track progress.

### App Evaluation

- **Category:** Productivity
- **Mobile:** Mobile only
- **Story:** Users can login/signup, view main screen of each day's habits (with filters by category and time of day), tap on habit to mark progress or completion, create habits with icons, title, description, and other details, and view weekly or monthly progress. 
- **Market:** Anyone 
- **Habit:** The nature of the app is to allow users to track their habits every day. The progress views also incentivize users to use the app consistently (e.g. maintain streaks).
- **Scope:** Can expand to a social app - user can add friends to complete habits together (e.g. workout together 1 hour daily) and remind each other to complete habits. Can also expand to include todos or calendar items that are not necessarily daily. 

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* Create new habit
* Update progress on today's habit
* Filter/sort habits by category, time of day, etc.
* Login
* Register a new account
* View habit progress of past week and month
* Edit and delete habits

**Optional Nice-to-have Stories**

* Calendar views of habit progress with color gradients, clickable days, etc. 
* Can tap on a habit to view a detail screen - description, progress on this one habit, insights like longest streak, % of days completed, etc.
* Track yesterday's (or custom day) habits (e.g. if user forgot to log something)
* Custom repeating habits (e.g. not every day, but every other day or every MWF, etc.)
* Notifications/reminders (?)

### 2. Screen Archetypes

* Login screen
    * User can login
* Registration screen
    * User can create a new account
* Habit list screen
    * View today's habits and sort/filter
    * Update progress on today's 
* Create habit screen
    * User can create a new habit
* Edit habit screen
    * User can edit or delete an existing habit
* Progress view screen
    * View weekly/monthly progress 

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* My habits
* Progress

**Flow Navigation** (Screen to Screen)

* Login screen
    * Habit list screen
* Registration screen
    * Habit list screen
* Habit list screen
    * Create habit screen
    * Edit habit screen
* Create habit screen
    * Habit list screen
* Edit habit screen
    * Habit list screen
* Progress view screen
    * None

## Wireframes
[App wireframes (Imgur)](https://imgur.com/a/f90ubTL)

### [BONUS] Digital Wireframes & Mockups

### [BONUS] Interactive Prototype

## Schema 

### Models

#### Habit

| Property         | Type                        | Description                                                                     |
|------------------|-----------------------------|---------------------------------------------------------------------------------|
| name             | String                      | name of habit                                                                   |
| author           | pointer to User             | user who created habit                                                          |
| icon             | File (or String?)           | icon (from fixed set of choices)                                                |
| createdAt        | DateTime                    | day of creation                                                                 |
| tag              | String                      | tag (from fixed set of choices)                                                 |
| qtyGoal          | Number                      | goal quantity (e.g. 50)                                                         |
| unit             | String                      | units of habit (e.g. minutes)                                                   |
| timeOfDay        | String                      | time of day of habit (morning/noon/afternoon/evening)                           |
| remindAtTime     | DateTime / null             | daily reminder time                                                             |
| remindAtLocation | pointer to Location / null  | daily reminder location. note only one of remindAtTime/Location can be non-null |
| progress         | pointer to associated class | pointer to class of entries of each day's progress on this habit                |
| (?) repeatOnDays | Array                       | which days to repeat this habit on (e.g. [F,T,F,T,T,F,F] = MWR)                 |

#### Location

| Property  | Type   | Description    |
|-----------|--------|----------------|
| name      | String | e.g. "Work"    |
| latitude  | String | e.g. "40.0 N"  |
| longitude | String | e.g. "-75.0 W" |

#### Habit1 (as many of these as there are habits)

| Property     | Type     | Description                |
|--------------|----------|----------------------------|
| date         | DateTime | a specific date            |
| qtyCompleted | Number   | completed amount           |
| qtyGoal      | Number   | goal amount                |
| pctCompleted | Number   | percentage of goal reached |

### Networking

* Login/Registration screen
   * POST: register new user
* Habit feed screen
   * GET: show habits
   * PUT: input progress on habit
   * DELETE: delete habit (e.g. by swiping?)
* Detail/Edit/Create habit screen
   * GET: show details
   * PUT: edit details
   * POST: create new habit
* Progress screen
   * GET: show progress for each day on a selected habit

- [Create basic snippets for each Parse network request]
- [OPTIONAL: List endpoints if using existing API such as Yelp]

## Further Documentation

* Added page and section headers to home RecyclerView by implementing multiple View types, dynamic getItemCount, etc.
* Faciliated querying with pointers, HashMap, and reduced querying with custom comparators. 
