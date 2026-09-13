package com.example.ui

import com.example.data.ReportEntry
import com.example.data.VillageEntry

class VillageStat {
    var activeM = 0
    var activeF = 0
    var passiveM = 0
    var passiveF = 0
}

object MonthlyReportGenerator {
    fun generateHtml(
        monthName: String,
        reports: List<ReportEntry>,
        villagesMap: Map<Int, List<VillageEntry>>
    ): String {
        var totalActive = 0
        var totalPassive = 0
        
        val villageStats = mutableMapOf<String, MutableMap<String, VillageStat>>()
        
        reports.forEach { report ->
            val isActive = report.designation.contains("आरोग्य सेवक") || 
                           report.designation.contains("आरोग्य सेविका") || 
                           report.designation.contains("आशा")
            
            val vList = villagesMap[report.id] ?: emptyList()
            vList.forEach { v ->
                val scMap = villageStats.getOrPut(report.upkendra) { mutableMapOf() }
                val vStat = scMap.getOrPut(v.villageName) { VillageStat() }
                
                if (isActive) {
                    vStat.activeM += v.maleCount
                    vStat.activeF += v.femaleCount
                    totalActive += v.sampleCount
                } else {
                    vStat.passiveM += v.maleCount
                    vStat.passiveF += v.femaleCount
                    totalPassive += v.sampleCount
                }
            }
        }
        
        val sbRows = java.lang.StringBuilder()
        var srNo = 1
        var grandActiveM = 0; var grandActiveF = 0
        var grandPassiveM = 0; var grandPassiveF = 0

        for ((upkendra, vMap) in villageStats) {
            for ((village, stat) in vMap) {
                val totM = stat.activeM + stat.passiveM
                val totF = stat.activeF + stat.passiveF
                val rowTot = totM + totF

                grandActiveM += stat.activeM; grandActiveF += stat.activeF
                grandPassiveM += stat.passiveM; grandPassiveF += stat.passiveF

                sbRows.append("<tr>")
                sbRows.append("<td>${srNo++}</td>")
                sbRows.append("<td>$upkendra</td>")
                sbRows.append("<td>$village</td>")
                sbRows.append("<td>${stat.activeM}</td><td>${stat.activeF}</td>")
                sbRows.append("<td>${stat.passiveM}</td><td>${stat.passiveF}</td>")
                sbRows.append("<td>$totM</td><td>$totF</td><td>$rowTot</td>")
                sbRows.append("</tr>")
            }
        }

        val finalTotM = grandActiveM + grandPassiveM
        val finalTotF = grandActiveF + grandPassiveF
        val finalGrandTot = finalTotM + finalTotF

        sbRows.append("<tr class='total-row'>")
        sbRows.append("<td colspan='3'>एकूण (Grand Total)</td>")
        sbRows.append("<td>$grandActiveM</td><td>$grandActiveF</td>")
        sbRows.append("<td>$grandPassiveM</td><td>$grandPassiveF</td>")
        sbRows.append("<td>$finalTotM</td><td>$finalTotF</td><td>$finalGrandTot</td>")
        sbRows.append("</tr>")

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; margin: 0; padding: 20px; font-size: 13px; }
                    .page-break { page-break-after: always; }
                    h1, h2, h3 { text-align: center; color: #333; }
                    table { width: 100%; border-collapse: collapse; margin-top: 20px; }
                    th, td { border: 1px solid #444; padding: 8px; text-align: center; }
                    th { background-color: #e0e0e0; font-weight: bold; }
                    .total-row { font-weight: bold; background-color: #f9f9f9; }
                    .title-page { height: 90vh; display: flex; flex-direction: column; justify-content: center; align-items: center; }
                    .title-page h1 { font-size: 32px; border-bottom: 2px solid #000; padding-bottom: 10px; margin-bottom: 20px; }
                    .title-page h2 { font-size: 24px; margin-bottom: 10px; }
                    .title-page h3 { font-size: 20px; }
                    .footer { margin-top: 50px; text-align: right; font-weight: bold; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="title-page">
                    <h1>प्राथमिक आरोग्य केंद्र, भादा</h1>
                    <h2>मासिक हिवताप अहवाल</h2>
                    <h3>माहे $monthName</h3>
                </div>
                <div class="page-break"></div>
                
                <h3>प्राथमिक आरोग्य केंद्र भादा गावनिहाय रक्त नमुना संकलन अहवाल (माहे: $monthName)</h3>
                <table>
                    <tr>
                        <th rowspan="2">अ.क्र.</th>
                        <th rowspan="2">उपकेंद्र</th>
                        <th rowspan="2">गाव</th>
                        <th colspan="2">ॲक्टिव्ह (Active)</th>
                        <th colspan="2">पॅसिव्ह (Passive)</th>
                        <th colspan="3">एकूण (Total)</th>
                    </tr>
                    <tr>
                        <th>पुरुष</th><th>स्त्री</th>
                        <th>पुरुष</th><th>स्त्री</th>
                        <th>पुरुष</th><th>स्त्री</th><th>एकूण</th>
                    </tr>
                    $sbRows
                </table>

                <div class="footer">
                    वैद्यकीय अधिकारी<br>
                    प्राथमिक आरोग्य केंद्र भादा
                </div>

                <div class="page-break"></div>
                
                <h3>राष्ट्रीय कीटकजन्य रोग नियंत्रण कार्यक्रम - संक्षिप्त अहवाल</h3>
                <table style="width: 60%; margin: 20px auto;">
                    <tr>
                        <th style="width: 70%;">तपशील</th>
                        <th>मासिक प्रगती</th>
                    </tr>
                    <tr><td style="text-align: left;">एकूण ॲक्टिव्ह रक्त नमुने</td><td>$totalActive</td></tr>
                    <tr><td style="text-align: left;">एकूण पॅसिव्ह रक्त नमुने</td><td>$totalPassive</td></tr>
                    <tr class="total-row"><td style="text-align: left;">एकूण तपासलेले रक्त नमुने</td><td>${totalActive + totalPassive}</td></tr>
                </table>
                
                <div class="footer">
                    वैद्यकीय अधिकारी<br>
                    प्राथमिक आरोग्य केंद्र भादा
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}
