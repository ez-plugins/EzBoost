[CENTER][SIZE=6][B]EzBoost[/B][/SIZE]
[SIZE=3]Configurable potion boosts with GUI activation, cooldowns, and optional Vault costs[/SIZE]
[SIZE=2]Renewed take on [URL='https://dev.bukkit.org/projects/redbull']RedBull[/URL][/SIZE]
[SIZE=2]Spigot / Paper / Bukkit 1.7–1.21.* • Optional Vault economy • Fully configurable boosts • GUI + command activation[/SIZE][/CENTER]

[SIZE=4][B]Why EzBoost?[/B][/SIZE]
[LIST]
[*][B]Clean, modern GUI[/B] – Let players browse available boosts, see cooldowns, costs, and status at a glance.
[IMG]https://i.ibb.co/1GgSfvWs/image.png[/IMG]
[*][B]Fully configurable boosts[/B] – Define any potion effects, amplifiers, durations, and permissions per boost.
[*][B]Cooldown control[/B] – Per-boost cooldowns with bypass permissions and optional per-type cooldown tracking.
[IMG]https://i.ibb.co/nsKmgK0H/image.png[/IMG]
[*][B]Economy-ready[/B] – Optional Vault integration to charge currency per boost activation.
[*][B]World restrictions[/B] – Allow/deny specific worlds for tight gameplay balancing.
[*][B]Player safety options[/B] – Keep boosts on death, reapply on join, and refund on failed activation.
[/LIST]

[SIZE=4][B]Feature Highlights[/B][/SIZE]
[LIST]
[*][B]GUI-first experience[/B] – Inventory menu with configurable filler, slot layout, and MiniMessage formatting.
[*][B]Fully configurable boosts[/B] – Define custom potion effects, amplifiers, durations, and permissions per boost.
[*][B]Multi-file configuration[/B] – Clean separation of settings, GUI, boosts, and more for easy management.
[*][B]Boost tokens[/B] – Grant boost token items with [icode]/ezboost give[/icode] for rewards, crates, or shops.
[*][B]Flexible limits[/B] – Clamp duration/amplifier ranges to keep boosts balanced.
[*][B]Per-boost cooldowns[/B] – Prevents abuse and enables balanced gameplay.
[*][B]World restrictions[/B] – Allow/deny boosts in specific worlds for tight gameplay tuning.
[*][B]Vault economy support[/B] – Optionally charge players for activating boosts.
[*][B]Live reload[/B] – Reload all configuration and messages at runtime with [icode]/ezboost reload[/icode].
[*][B]Friendly messaging[/B] – Customizable MiniMessage strings for actionbar/status feedback.
[*][B]Command hooks[/B] – Run console commands on enable/disable/toggle per boost.
[*][B]Player-friendly behavior[/B] – Reapply boosts on join, keep on death, and refund on failed activation.
[/LIST]

[SIZE=4][B]Quick Start[/B][/SIZE]
[LIST]
[*]Drop [icode]EzBoost.jar[/icode] into [icode]plugins/[/icode], then start your Paper server (1.20+).
[*]Edit [icode]plugins/EzBoost/boosts.yml[/icode], [icode]gui.yml[/icode], and related config files to configure boosts, cooldowns, costs, and GUI slots.
[*]Use [icode]/boost[/icode] to open the GUI, or [icode]/boost <boostKey>[/icode] for direct activation.
[*]Grant permissions like [icode]ezboost.boost.speed[/icode] to control access per boost.
[/LIST]

[B]Commands & Permissions[/B]
[table]
[tr][th]Command[/th][th]Description[/th][th]Permission[/th][/tr]
[tr][td]/boost[/td][td]Open the boosts GUI or show usage.[/td][td]ezboost.use[/td][/tr]
[tr][td]/boost <boostKey>[/td][td]Activate a specific boost directly.[/td][td]ezboost.use + boost permission[/td][/tr]
[tr][td]/ezboost reload[/td][td]Reload configuration and messages.[/td][td]ezboost.reload[/td][/tr]
[tr][td]/ezboost give <player> <boostKey> [amount][/td][td]Give boost token items.[/td][td]ezboost.give[/td][/tr]
[/table]

[SIZE=4][B]Setup Guide[/B][/SIZE]
[spoiler="Installation & Configuration"]
[B]Requirements[/B]
[table]
[tr][th]Requirement[/th][th]Notes[/th][/tr]
[tr][td]Java 17+[/td][td]Recommended runtime for Paper 1.20–1.21 servers.[/td][/tr]
[tr][td]Paper/Purpur 1.20–1.21[/td][td]Built for modern server APIs.[/td][/tr]
[tr][td]Vault (optional)[/td][td]Required only if you enable economy costs in [icode]economy.enabled[/icode].[/td][/tr]
[/table]

[B]Configuration Overview[/B]
[LIST]
[*][B]Boost definitions[/B] – Add or edit boosts in [icode]boosts.yml[/icode] with effects, duration, cooldown, cost, and permissions. See [docs/boosts.md](https://github.com/ez-plugins/ezboost/blob/main/docs/boosts.md) for a full reference.
[*][B]GUI layout[/B] – Configure size, filler, lore templates, and per-boost slot placement in [icode]gui.yml[/icode].
[*][B]Limits[/B] – Clamp duration/amplifier ranges to keep effects balanced in [icode]limits.yml[/icode].
[*][B]World rules[/B] – Use [icode]worlds.allow-list[/icode] or [icode]worlds.deny-list[/icode] for restrictions in [icode]worlds.yml[/icode].
[*][B]Behavior toggles[/B] – Replace active boosts, reapply on join, keep on death, and refund on fail in [icode]settings.yml[/icode].
[/LIST]
[/spoiler]

[SIZE=4][B]Tip: Boost Permissions[/B][/SIZE]
[LIST]
[*]Each boost can declare its own permission, e.g. [icode]ezboost.boost.speed[/icode].
[*]Give ranks unique boosts while keeping the GUI layout consistent for everyone.
[/LIST]

[CENTER][URL='https://www.spigotmc.org/resources/authors/shadow48402.25936/'][IMG]https://i.ibb.co/PzfjNjh0/ezplugins-try-other-plugins.png[/IMG][/URL][/CENTER]


[SIZE=4][B]More Information & Documentation[/B][/SIZE]
[LIST]
[*][URL='https://github.com/ez-plugins/EzBoost']EzBoost GitHub Repository[/URL] – Source code, issues, and latest updates.
[*][URL='https://github.com/ez-plugins/EzBoost/blob/main/docs/config.md']Configuration Guide[/URL] – Full details on all config options.
[*][URL='https://github.com/ez-plugins/EzBoost/blob/main/docs/boosts.md']Boosts Reference[/URL] – YAML format and boost customization.
[*][URL='https://github.com/ez-plugins/EzBoost/blob/main/docs/gui.md']GUI Customization[/URL] – How to configure the boost GUI.
[/LIST]