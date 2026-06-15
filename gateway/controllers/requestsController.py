from fastapi import APIRouter, Header, Query
from typing import Optional
from controllers._spring_proxy import spring_request

router = APIRouter(prefix="/requests")


@router.get("")
async def list_requests(status: Optional[str] = Query(None), Token: str = Header(...)):
    params = {"status": status} if status else None
    return await spring_request("GET", "/requests", token=Token, params=params)


@router.post("")
async def create_request(body: dict, Token: str = Header(...)):
    return await spring_request("POST", "/requests", json=body, token=Token)


@router.put("/{request_id}/approve")
async def approve_request(request_id: int, body: Optional[dict] = None, Token: str = Header(...)):
    return await spring_request("PUT", f"/requests/{request_id}/approve", json=body, token=Token)


@router.put("/{request_id}/reject")
async def reject_request(request_id: int, body: Optional[dict] = None, Token: str = Header(...)):
    return await spring_request("PUT", f"/requests/{request_id}/reject", json=body, token=Token)


@router.put("/{request_id}/cancel")
async def cancel_request(request_id: int, Token: str = Header(...)):
    return await spring_request("PUT", f"/requests/{request_id}/cancel", token=Token)
