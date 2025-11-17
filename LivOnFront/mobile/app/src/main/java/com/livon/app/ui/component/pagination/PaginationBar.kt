package com.livon.app.ui.component.pagination

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livon.app.ui.theme.LivonTheme
import com.livon.app.ui.theme.Main

/**
 * 페이지네이션 바 컴포저블
 * 
 * < 1 2 3 ... > 형태의 페이지 번호 표시
 * - 현재 페이지는 볼드 처리 및 Main 색상
 * - 클릭 시 onPageChange 콜백 호출
 * 
 * @param currentPage 현재 페이지 (1부터 시작)
 * @param totalPages 전체 페이지 수
 * @param onPageChange 페이지 변경 콜백 (1부터 시작하는 페이지 번호 전달)
 * @param modifier Modifier
 */
@Composable
fun PaginationBar(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (totalPages <= 1) return // 페이지가 1개 이하면 표시하지 않음
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 이전 페이지 버튼 (<)
        if (currentPage > 1) {
            Text(
                text = "<",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .clickable { onPageChange(currentPage - 1) }
                    .padding(horizontal = 8.dp),
                textAlign = TextAlign.Center
            )
        } else {
            // 첫 페이지일 때는 비활성화 (공간 확보를 위해 투명하게 표시)
            Text(
                text = "<",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                modifier = Modifier.padding(horizontal = 8.dp),
                textAlign = TextAlign.Center
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // 페이지 번호 표시
        when {
            totalPages <= 7 -> {
                // 페이지가 7개 이하면 모두 표시: 1 2 3 4 5 6 7
                (1..totalPages).forEach { page ->
                    PageNumberButton(
                        page = page,
                        isSelected = page == currentPage,
                        onClick = { onPageChange(page) }
                    )
                    if (page < totalPages) {
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
            currentPage <= 3 -> {
                // 현재 페이지가 앞쪽: 1 2 3 ... 10
                (1..3).forEach { page ->
                    PageNumberButton(
                        page = page,
                        isSelected = page == currentPage,
                        onClick = { onPageChange(page) }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = "...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                PageNumberButton(
                    page = totalPages,
                    isSelected = false,
                    onClick = { onPageChange(totalPages) }
                )
            }
            currentPage >= totalPages - 2 -> {
                // 현재 페이지가 뒤쪽: 1 ... 8 9 10
                PageNumberButton(
                    page = 1,
                    isSelected = false,
                    onClick = { onPageChange(1) }
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                ((totalPages - 2)..totalPages).forEach { page ->
                    PageNumberButton(
                        page = page,
                        isSelected = page == currentPage,
                        onClick = { onPageChange(page) }
                    )
                    if (page < totalPages) {
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
            else -> {
                // 현재 페이지가 중간: 1 ... 4 5 6 ... 10
                PageNumberButton(
                    page = 1,
                    isSelected = false,
                    onClick = { onPageChange(1) }
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                ((currentPage - 1)..(currentPage + 1)).forEach { page ->
                    PageNumberButton(
                        page = page,
                        isSelected = page == currentPage,
                        onClick = { onPageChange(page) }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = "...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                PageNumberButton(
                    page = totalPages,
                    isSelected = false,
                    onClick = { onPageChange(totalPages) }
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // 다음 페이지 버튼 (>)
        if (currentPage < totalPages) {
            Text(
                text = ">",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .clickable { onPageChange(currentPage + 1) }
                    .padding(horizontal = 8.dp),
                textAlign = TextAlign.Center
            )
        } else {
            // 마지막 페이지일 때는 비활성화 (공간 확보를 위해 투명하게 표시)
            Text(
                text = ">",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                modifier = Modifier.padding(horizontal = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PageNumberButton(
    page: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        text = page.toString(),
        style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 16.sp
        ),
        color = if (isSelected) Main else MaterialTheme.colorScheme.onBackground,
        modifier = modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        textAlign = TextAlign.Center
    )
}

/* -------------------- Preview -------------------- */

@Preview(showBackground = true, name = "PaginationBar - Page 1 of 7")
@Composable
private fun PaginationBarPreview_Page1() {
    LivonTheme {
        PaginationBar(
            currentPage = 1,
            totalPages = 7,
            onPageChange = {}
        )
    }
}

@Preview(showBackground = true, name = "PaginationBar - Page 4 of 10")
@Composable
private fun PaginationBarPreview_Page4() {
    LivonTheme {
        PaginationBar(
            currentPage = 4,
            totalPages = 10,
            onPageChange = {}
        )
    }
}

@Preview(showBackground = true, name = "PaginationBar - Page 9 of 10")
@Composable
private fun PaginationBarPreview_Page9() {
    LivonTheme {
        PaginationBar(
            currentPage = 9,
            totalPages = 10,
            onPageChange = {}
        )
    }
}

@Preview(showBackground = true, name = "PaginationBar - Single Page")
@Composable
private fun PaginationBarPreview_SinglePage() {
    LivonTheme {
        PaginationBar(
            currentPage = 1,
            totalPages = 1,
            onPageChange = {}
        )
    }
}

