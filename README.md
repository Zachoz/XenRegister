XenRegister
==============

A simple [bukkit/spigot](http://bukkit.org) plugin to allow registration on [XenForo](http://xenforo.com/) powered forums using in-game commands. Originally desgined for [OresomeCraft](http://oresomecraft.com).

### Setup
This plugin requires [XenAPI](http://github.com/Contex/XenAPI) (https://github.com/Contex/XenAPI) to be installed. Place the XenAPI 'api.php' file in the root folder of your XenForo installation.

To make sure this works 100%, make the following edits to api.php:

Ignore the ```<?php ?>``` tags in the following code snippets. They're simply there for syntax highlighting.

#### Set API key
In api.php, change the API key:
```php
<?php
$options = array('api_key' => 'Change this to some generated hash');
?>
```

#### Quick 400 error fix
Change:
```php
<?php
    // Throw a 400 error.
    header('HTTP/ 400 API error'); // Line ~662
?>
```
To: (comment out line)
```php
<?php
     //header('HTTP/ 400 API error');
?>
```

#### Send user a randomly generated password
To generate a random password for the user after their account has been created, make the following edits:
Make the following edit to AccountConfirmation.php (SiteDir/library/XenForo/ControllerPublic/AccountConfirmation.php)

At the bottom of actionEmail() add:
```php
<?php
    // If registered by /register send them a new password!
	XenForo_Model::create('XenForo_Model_UserConfirmation')->resetPassword($user['user_id'], true);
?>
```

It should look something like this after doing that:

```php
<?php
    // If registered by /register send them a new password!
	XenForo_Model::create('XenForo_Model_UserConfirmation')->resetPassword($user['user_id'], true);

	return $this->responseView('XenForo_ViewPublic_Register_Confirm', 'register_confirm', $viewParams);
?>
```

#### Update templates
Make sure you modify your templates (user registration, emails, etc) to let the user know that their password will be sent to them.
This can be changed in the 'register_confirm' template. (ACP -> Templates -> 'register_confirm')

#### Custom user field for the user's IGN
I recommend you create a custom user field (google it) to store the user's Minecraft username just in case. If called 'minecraftusername', this plugin will automatically set it to their in-game name.

#### That's about it for the XenForo configuration
If these steps are followed correctly, this should work fine. I recommend you disable the "Sign up" button/ability to sign up via the website if you want to be able to confirm that the forum account account belongs to the associated Minecraft player.

### Plugin configuration

```yaml
site: "http://oresomecraft.com/" # URL the forum runs on. The trailing / is important!
apihash: "42424u22ioe824280" # The API secret set in your api.php
```

Services like CloudFlare may interfere with accessing the site, so make sure the URL goes directly to your web server.

### Compiling
You must have [Apache Maven](http://maven.apache.org) installed to compile.

To compile, use the following command:

```mvn clean install```
