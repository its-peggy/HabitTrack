Original App Design Project - README Template
===

# Habit Tracker

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

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

- [x] Login
- [x] Register a new account
- [x] View list of all current habits
- [x] Filter/sort habits by category, time of day, etc.
- [x] Create new habit
- [x] Edit habits
- [x] Update progress on today's habit
- [x] View overall habit progress of past month

**Optional Nice-to-have Stories**

- [x] Time-based reminders
- [x] Location-based reminders (partially complete as of July 29 morning)
- [x] Calendar view of habit progress with color gradients indicating progress
- [x] Can tap on a habit to view a detail screen 
- [ ] View habit insights - current streak, longest streak, % of days completed, etc.
- [x] Custom repeating habits (e.g. not every day, but every other day or every MWF, etc.)
- [ ] Support for non-quantity-based habits (e.g. "skip dessert", "pack lunch for work")
- [x] Delete habits

### 2. Screen Archetypes

* Start/Welcome screen
    * User can choose to login or register 
* Login screen
    * User can login
* Registration screen
    * User can create a new account
* Habit list screen
    * View today's habits and sort/filter
    * Update progress on today's habits
* Create habit screen
    * User can create a new habit
* Detail/Edit habit screen
    * User can view habit details and/or edit habit
* Progress view screen
    * View weekly/monthly progress 
* Profile screen
    * User can log out
* Address management screen
    * User can enter addresses (Home, Work, etc.)

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* My habits
* Progress
* Profile

**Flow Navigation** (Screen to Screen)

* Start screen
    * Login screen
    * Registration screen
* Login screen
    * Habit list screen
* Registration screen
    * Habit list screen
* Habit list screen
    * Create habit screen
    * Detail/Edit habit screen
* Create habit screen
    * Habit list screen
* Edit habit screen
    * Habit list screen
* Progress view screen
    * None
* Profile screen
    * Address management screen
* Address management screen
    * Profile screen

## Wireframes
[App wireframes (Imgur)](https://imgur.com/a/f90ubTL)

## Schema 

### Models

#### Habit

| Property         | Type                        | Description                                                                     |
|------------------|-----------------------------|---------------------------------------------------------------------------------|
| user             | pointer to User             | user who created habit                                                          |
| name             | String                      | name of habit                                                                   |
| icon             | File                        | icon (from fixed set of choices)                                                |
| createdAt        | DateTime                    | day of creation                                                                 |
| tag              | String                      | tag (from fixed set of choices)                                                 |
| qtyGoal          | Number                      | goal quantity (e.g. 50)                                                         |
| unit             | String                      | units of habit (e.g. minutes)                                                   |
| timeOfDay        | String                      | time of day of habit (morning/noon/afternoon/evening)                           |
| streak           | Number                      | current habit streak                                                            |
| longestStreak    | Number                      | longest habit streak                                                            |
| remindAtTime     | DateTime / null             | daily reminder time                                                             |
| remindAtLocation | pointer to Location / null  | daily reminder location. note only one of remindAtTime/Location can be non-null |
| progress         | pointer to Profress         | pointer to entry of current day's progress on this habit                        |

#### Location

| Property  | Type     | Description          |
|-----------|----------|----------------------|
| user      | User     | user                 |
| name      | String   | e.g. "Work"          |
| location  | GeoPoint | location of interest |

#### Progress

| Property     | Type     | Description                                 |
|--------------|----------|---------------------------------------------|
| user         | User     | user                                        |
| date         | String   | date this entry corresponds to (yyyy-MM-dd) |
| qtyCompleted | Number   | completed amount                            |
| qtyGoal      | Number   | goal amount                                 |
| pctCompleted | Number   | percentage of goal reached                  |
| completed    | Boolean  | whether qtyCompleted == qtyGoal             |
| habit        | Habit    | Habit that this progress corresponds to     |

### Networking

* Login/Registration screen
   * POST: register new user
* Habit feed screen
   * GET: show habits
   * PUT: input progress on habit
   * DELETE: delete habit 
* Create habit
   * POST: create new habit
* Detail/Edit habit screen
   * GET: show details
   * PUT: edit details
* Progress screen
   * GET: show progress for each day on a selected habit
* Address management screen
   * POST: create new addresses/locations of interest
