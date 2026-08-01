param(
    [string]$MapFunctionsRoot = (Join-Path $PSScriptRoot '..\..\..\test\Final_Paradox_v1.1.15\datapacks\luisb1202-functions\data\luisb1202\functions'),
    [string]$OutputFile = (Join-Path $PSScriptRoot '..\..\src\main\java\io\github\finalparadox\ability\NightfallSlashFrames.java')
)

$ErrorActionPreference = 'Stop'
$culture = [System.Globalization.CultureInfo]::InvariantCulture
$sourceRoot = [System.IO.Path]::GetFullPath($MapFunctionsRoot)
$outputPath = [System.IO.Path]::GetFullPath($OutputFile)
$comboRoot = Join-Path $sourceRoot 'bossfight\b9\h1\combo1\particulas'
$pattern = '^execute positioned \^([^\s]*) \^([^\s]*) \^([^\s]*) rotated ~([^\s]+) ~([^\s]+) run function luisb1202:bossfight/b9/h1/combo1/particulas/trail$'

function Parse-Number([string]$text) {
    if ([string]::IsNullOrEmpty($text)) {
        return 0.0
    }
    return [double]::Parse($text, $culture)
}

function Java-Float([double]$value) {
    if ([Math]::Abs($value) -lt 0.0000000001) {
        return '0.0F'
    }
    return $value.ToString('0.################', $culture) + 'F'
}

$builder = [System.Text.StringBuilder]::new()
[void]$builder.AppendLine('package io.github.finalparadox.ability;')
[void]$builder.AppendLine()
[void]$builder.AppendLine('/**')
[void]$builder.AppendLine(' * Generated from Final Paradox B9 h1/combo1 particle frame commands.')
[void]$builder.AppendLine(' * Regenerate with tools/reconstruction/generate_nightfall_slash_frames.ps1.')
[void]$builder.AppendLine(' */')
[void]$builder.AppendLine('final class NightfallSlashFrames {')
[void]$builder.AppendLine('    private NightfallSlashFrames() {}')
[void]$builder.AppendLine()
[void]$builder.AppendLine('    record Sample(float left, float up, float forward, float yaw, float pitch) {}')
[void]$builder.AppendLine()
[void]$builder.AppendLine('    static final Sample[][] FRAMES = {')

$counts = [System.Collections.Generic.List[int]]::new()
for ($strike = 1; $strike -le 3; $strike++) {
    for ($pack = 1; $pack -le 3; $pack++) {
        $sourceFile = Join-Path $comboRoot "golpe_$strike\pack$pack.mcfunction"
        $samples = [System.Collections.Generic.List[object]]::new()
        foreach ($line in Get-Content -LiteralPath $sourceFile) {
            if ($line -match $pattern) {
                $samples.Add(@(
                    (Parse-Number $Matches[1]),
                    (Parse-Number $Matches[2]),
                    (Parse-Number $Matches[3]),
                    (Parse-Number $Matches[4]),
                    (Parse-Number $Matches[5])
                ))
            }
        }
        if ($samples.Count -eq 0) {
            throw "No slash samples parsed from $sourceFile"
        }
        $counts.Add($samples.Count)
        [void]$builder.AppendLine("        { // strike $strike, source frame $pack")
        foreach ($sample in $samples) {
            $values = $sample | ForEach-Object { Java-Float $_ }
            [void]$builder.AppendLine("            new Sample($($values -join ', ')),")
        }
        [void]$builder.AppendLine('        },')
    }
}

[void]$builder.AppendLine('    };')
[void]$builder.AppendLine('}')

$expected = @(54, 55, 55, 55, 35, 30, 21, 24, 25)
if (($counts -join ',') -ne ($expected -join ',')) {
    throw "Unexpected source frame counts: $($counts -join ', ')"
}

[System.IO.Directory]::CreateDirectory([System.IO.Path]::GetDirectoryName($outputPath)) | Out-Null
[System.IO.File]::WriteAllText($outputPath, $builder.ToString(), [System.Text.UTF8Encoding]::new($false))
Write-Output "Generated $outputPath from $($counts.Count) frames and $(($counts | Measure-Object -Sum).Sum) samples."
