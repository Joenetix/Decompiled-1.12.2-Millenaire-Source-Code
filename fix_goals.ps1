
$files = Get-ChildItem -Path "src\main\java\org\millenaire\common\goal" -Recurse -Filter "*.java"

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    $originalContent = $content

    # Replace import
    $content = $content -replace "import org.millenaire.entities.Citizen;", "import org.millenaire.common.entity.MillVillager;"

    # Replace method signatures (Citizen -> MillVillager)
    $methods = @(
        "isPossible", 
        "performAction", 
        "priority", 
        "range", 
        "actionDuration", 
        "getHeldItemsTravelling", 
        "getHeldItemsDestination", 
        "getHeldItemsOffHandTravelling", 
        "getHeldItemsOffHandDestination",
        "labelKey",
        "labelKeyWhileTravelling",
        "sentenceKey" 
    )

    foreach ($method in $methods) {
        # Regex to handle whitespace and parameter names
        # public type methodName(Citizen var1)
        # Matches: public int priority(Citizen citizen)
        $content = $content -replace "($method\s*\(\s*)Citizen(\s+\w+\s*\))", "`$1MillVillager`$2"
    }
    
    # Also handle the @Override annotation which might precede it
    # AND handle cases where it casts (MillVillager)citizen inside
    # We leave the casts for now as they are redundant but valid java.
    
    # Special Fixes for specific files if needed
    
    if ($content -ne $originalContent) {
        Set-Content -Path $file.FullName -Value $content -NoNewline
        Write-Host "Updated $($file.Name)"
    }
}
