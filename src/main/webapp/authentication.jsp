
<%@ page pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <title>Authentication</title>
        <link rel="stylesheet" href="inc/authentication.css" />
    </head>
    <body>
    	<header>
    		<h1>Authentication</h1>
    	</header>
    	<!--  -->
    	<div id="formulaire">
    		<form method="post" action="authentication">
                <fieldset>
  						<input type="text" placeholder="EHR Username" id="EHRuid" name="EHRuid" required value="" size="40" maxlength="60" />
	                	<br />
	                    <br />
	                    <br />
	                    <input type="password" placeholder="password" id="motdepasse" name="motdepasse" required value="" size="40" maxlength="60" />
	                    <br />
                </fieldset>
                <div class="ok">
	                <input type="submit" value="Validate" class="bouton" />
	                 <br />
                </div>
            </form>
        </div>
    </body>
</html>

