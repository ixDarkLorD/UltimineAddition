---
icon: material/calendar-star
description: One challenge a day for everyone
---

# Daily Challenge

Every day the server picks one challenge that everyone shares. It needs no Mining Skill Card and no Skills Record: do what it asks with the right tool and it counts.

## How it works

- A notice shows the challenge a few seconds after you join, when you start it and when you are halfway.
- `/ultimine_addition challenge` shows it any time, with your progress and when the next one comes.
- Blocks placed by players don't count, as with card challenges. Creative mode doesn't count either.
- A new challenge comes at midnight by the server's clock. Progress on the old one is dropped.

## The reward

Finishing it gives experience and a few minutes of Mine-Go Juice for the challenge's tool, so you can Ultimine with that tool even before your card earns it.

## For servers and packs

In the [server settings](packs/configuration.md#server-settings), `progression.timed_challenge` is `daily`, `weekly` or `off`. A weekly challenge asks for more and pays four times as much. `timed_challenge_experience` and `timed_challenge_juice` set the reward.

Data packs can add their own reward with `"when": "timed"`: see [Rewards](../docs/data-packs.md#reward).
