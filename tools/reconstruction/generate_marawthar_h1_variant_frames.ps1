param(
    [string]$MapFunctionsRoot = (Join-Path $PSScriptRoot '..\..\..\test\Final_Paradox_v1.1.15\datapacks\luisb1202-functions\data\luisb1202\functions'),
    [string]$OutputFile = (Join-Path $PSScriptRoot '..\..\src\main\java\io\github\finalparadox\entity\MarawTharH1VariantFrames.java')
)

$ErrorActionPreference = 'Stop'
$culture = [System.Globalization.CultureInfo]::InvariantCulture
$sourceRoot = [System.IO.Path]::GetFullPath($MapFunctionsRoot)
$outputPath = [System.IO.Path]::GetFullPath($OutputFile)
$h1Root = Join-Path $sourceRoot 'bossfight\b9\h1'
$pattern = '^execute positioned \^([^\s]*) \^([^\s]*) \^([^\s]*) rotated ~([^\s]*) ~([^\s]*) run function luisb1202:bossfight/b9/h1/combo2/particulas/trail2$'

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

$groups = [ordered]@{
    COMBO2 = @(
        'combo2\particulas\golpe_1\pack1.mcfunction',
        'combo2\particulas\golpe_1\pack2.mcfunction',
        'combo2\particulas\golpe_2\pack1.mcfunction',
        'combo2\particulas\golpe_2\pack3.mcfunction',
        'combo2\particulas\golpe_2\pack2.mcfunction'
    )
    COMBO3 = @(
        'combo3\particulas\golpe_1\pack1.mcfunction',
        'combo3\particulas\golpe_1\pack2.mcfunction'
    )
    COMBO4 = @(
        'combo4\particulas\golpe_1\pack1.mcfunction',
        'combo4\particulas\golpe_1\pack2.mcfunction'
    )
}

$expectedCounts = @(91, 91, 55, 68, 57, 85, 97, 93, 87)
$actualCounts = [System.Collections.Generic.List[int]]::new()
$builder = [System.Text.StringBuilder]::new()
[void]$builder.AppendLine('package io.github.finalparadox.entity;')
[void]$builder.AppendLine()
[void]$builder.AppendLine('/**')
[void]$builder.AppendLine(' * Literal B9 h1 combo2, combo3, and combo4 slash-particle samples.')
[void]$builder.AppendLine(' * Regenerate with tools/reconstruction/generate_marawthar_h1_variant_frames.ps1.')
[void]$builder.AppendLine(' */')
[void]$builder.AppendLine('final class MarawTharH1VariantFrames {')
[void]$builder.AppendLine('    private MarawTharH1VariantFrames() {}')
[void]$builder.AppendLine()
[void]$builder.AppendLine('    record Sample(float left, float up, float forward, float yaw, float pitch) {}')
[void]$builder.AppendLine()

foreach ($group in $groups.GetEnumerator()) {
    [void]$builder.AppendLine("    static final Sample[][] $($group.Key) = {")
    foreach ($relativePath in $group.Value) {
        $sourceFile = Join-Path $h1Root $relativePath
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
        $actualCounts.Add($samples.Count)
        [void]$builder.AppendLine("        { // $relativePath")
        foreach ($sample in $samples) {
            $values = $sample | ForEach-Object { Java-Float $_ }
            [void]$builder.AppendLine("            new Sample($($values -join ', ')),")
        }
        [void]$builder.AppendLine('        },')
    }
    [void]$builder.AppendLine('    };')
    [void]$builder.AppendLine()
}

[void]$builder.AppendLine('}')

if (($actualCounts -join ',') -ne ($expectedCounts -join ',')) {
    throw "Unexpected source frame counts: $($actualCounts -join ', ')"
}

[System.IO.Directory]::CreateDirectory([System.IO.Path]::GetDirectoryName($outputPath)) | Out-Null
[System.IO.File]::WriteAllText($outputPath, $builder.ToString(), [System.Text.UTF8Encoding]::new($false))
Write-Output "Generated $outputPath from $($actualCounts.Count) frames and $(($actualCounts | Measure-Object -Sum).Sum) samples."
