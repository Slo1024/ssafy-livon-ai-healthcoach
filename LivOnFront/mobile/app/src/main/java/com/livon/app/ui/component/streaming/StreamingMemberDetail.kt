package com.livon.app.ui.component.streaming

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.livon.app.R // R.drawable.cancle에 접근하기 위해 import
import com.livon.app.ui.theme.Qna
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex

data class BioInfo(val type: String)
data class QAItem(val question: String)

@Composable
fun StreamingMemberDetail(
    memberName: String,
    bioInfoList: List<BioInfo>,
    bottomText: String,
    qaList: List<QAItem>,
    onClose: () -> Unit = {},
    closeIconResId: Int = R.drawable.cancle,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(287.dp)
            .height(486.dp)
            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
        color = Color.Transparent,
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopEnd
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 16.dp)
                    .padding(top = 20.dp, bottom = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${memberName}님",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = onClose) {
                        Icon(
                            painter = painterResource(id = closeIconResId),
                            contentDescription = "닫기",
                            tint = Color.White
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Divider(color = Color.DarkGray, thickness = 1.dp)

                Text(
                    text = "생체 정보",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 16.dp)
                )

                Spacer(Modifier.height(8.dp))

                BioInfoGrid(bioInfoList)

                Spacer(Modifier.height(16.dp))

                Text(
                    text = bottomText,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(Modifier.height(16.dp))
                Divider(color = Color.DarkGray, thickness = 1.dp)

                Text(
                    text = "Q&A",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 16.dp)
                )

                Spacer(Modifier.height(8.dp))

                Column(Modifier.fillMaxWidth()) {
                    qaList.forEach { item ->
                        QATextBox(item)
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }

            
        }
    }
}

@Composable
fun BioInfoGrid(bioInfoList: List<BioInfo>) {
    val chunkSize = 3
    bioInfoList.chunked(chunkSize).forEach { rowItems ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rowItems.forEach { info ->
                BioInfoBox(info = info, modifier = Modifier.weight(1f))
            }
            repeat(chunkSize - rowItems.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun BioInfoBox(info: BioInfo, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(Qna, RoundedCornerShape(4.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = info.type,
            fontSize = 11.sp,
            color = Color.Black
        )
    }
}

@Composable
fun QATextBox(item: QAItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Qna, RoundedCornerShape(4.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "Q. ${item.question}",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
    }
}