<img width="99.9%" src="https://raw.githubusercontent.com/mishakorzik/mishakorzik.menu.io/master/img/IpHack/20210822_200816.png"/>

<p align="center">
<a href="https://github.com/mishakorzik/IpHack"><img title="Version" src="https://img.shields.io/badge/Build-done-darkred?style=for-the-badge&logo="></a>
<a href="https://github.com/mishakorzik/IpHack/blob/main/LICENSE"><img title="License" src="https://img.shields.io/badge/Apache-License 2.0-red?style=for-the-badge&logo=apache"></a>
<a href=""><img title="Python" src="https://img.shields.io/badge/Made in-Ukraine-red?style=for-the-badge&logo=None"></a>
<a href="https://github.com/mishakorzik"><img title="Report" src="https://img.shields.io/badge/Copyring-2022-orange?style=for-the-badge&logo=github"></a>
<a href="https://github.com/mishakorzik"><img title="Autor" src="https://img.shields.io/badge/Author-mishakorzik-yellow?style=for-the-badge&logo=github"></a>

</p>

<p align="center">
• <a href="https://github.com/mishakorzik/IpHack/blob/main/LICENSE">License</a>
• <a href="https://github.com/mishakorzik/IpHack/issues">Issues</a>
• <a href="https://github.com/mishakorzik/IpHаck/projects">Project</a>
• <a href="https://github.com/mishakorzik/IpHack/wiki">Wikipedia</a> •

</p>

> IpHack: is a tracking tool for both IP location and tracking testing.

---

**Track Location With Live Address And City in Termux**

> Я не несу ответственности за ваши действия. Скачивая программное обеспечение из этого репозитория, вы соглашаетесь с [лицензией](https://github.com/mishakorzik/IpHack/blob/main/LICENSE).

----
## New Features

**1) New Functions**
- Now you can find out in which country the person is.

**2) Detail Information**
- You can find out a lot of information about IP.

**3) Stability**
- Uses multiple APIs to retrieve information.
-----
## Install

**with pip**
```bash
pip install iphack
```

## Usage
**Ip Address**

```python
# ip address tracking
from iphack import ip
ip.address("ip", output_json=False)

# domain ip address tracking
from iphack import ip
ip.domain("google.com")

# my ip address
from iphack import ip
ip.my(output_json=False)
```

**Websites**

```python
# Check amazon subdomains
from iphack import ip
ip.subdomains("amazon.com")

# Check amazon directories
from iphack import ip
ip.directory("amazon.com")
```

## Screenshot

<img width="99.9%" src="https://raw.githubusercontent.com/mishakorzik/IpHack/main/Screenshot_2022-08-13_21-33-49.png"/>

## I recommend watching

1.<a href="https://github.com/mishakorzik/ExploitAddr">ExploitAddr - Find website IPs behind cloudflare</a>

2.<a href="https://github.com/mishakorzik/AdminHack">AdminHack - Hacking bad sites</a>

3.<a href="https://github.com/mishakorzik/UserFinder">UserFinder - OSINT tool for finding profiles by username</a>

4.<a href="https://github.com/mishakorzik/AllHackingTools">AllHackingTools - System for large hacking</a>

5.<a href="https://github.com/mishakorzik/py-ddoser">Py-ddoser - Hing ddos bad sites</a>
