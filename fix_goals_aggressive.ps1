$goalPath = "C:\Millenaire Revived v0\src\main\java\org\millenaire\common\goal"
$files = Get-ChildItem -Path $goalPath -Recurse -Filter "*.java"

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    $originalContent = $content

    # Replace faulty imports
    $content = $content -replace "import org\.millenaire\.common\.utilities\.WorldUtilities;", "import org.millenaire.utilities.WorldUtilities;"
    $content = $content -replace "import org\.millenaire\.common\.item\.MillItems;", "import org.millenaire.core.MillItems;"

    # Replace import for Citizen
    $content = $content -replace "import org\.millenaire\.entities\.Citizen;", "import org.millenaire.common.entity.MillVillager;"
    
    # Replace class usage (Citizen -> MillVillager)
    # Avoid replacing valid substrings if any (unlikely for Citizen)
    # We use word boundary \b to be safe
    $content = $content -replace "\bCitizen\b", "MillVillager"

    if ($content -ne $originalContent) {
        Set-Content -Path $file.FullName -Value $content
        Write-Host "Updated $($file.Name)"
    }
}
